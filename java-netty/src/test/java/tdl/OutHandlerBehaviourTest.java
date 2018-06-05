package tdl;

import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class OutHandlerBehaviourTest {

    static ChannelOutboundHandlerAdapter outboundHandlerNotPassingOnPromise() {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ctx.write(msg);
            }
        };
    }

    private static ChannelOutboundHandlerAdapter outboundHandlerPassingOnPromise() {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
               ctx.write(msg, promise);
            }
        };
    }

    private static ChannelOutboundHandlerAdapter outboundHandlerWithNewPromiseInWrite() {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ctx.write(msg, ctx.newPromise());
            }
        };
    }

    private static ChannelOutboundHandlerAdapter errorThrowingOutHandler() {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                throw new RuntimeException("boom, shake the room");
            }
        };
    }

    private static ChannelInboundHandlerAdapter mirrorInHandler() {
        return new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ctx.writeAndFlush(msg);
            }
        };
    }

    @NotNull
    private static String okMessage(Object msg) {
        return "OK:" + msg;
    }

    @Test
    public void netty_invokes_the_next_handlers_write_with_its_written_message() {
        final ChannelOutboundHandlerAdapter aHandler = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ctx.write(okMessage(msg));
            }
        };

        final EmbeddedChannel channel = new EmbeddedChannel(
                aHandler,
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        channel.flushOutbound();
        String result = channel.readOutbound();
        assertThat(result).isEqualTo(okMessage("hello"));
    }

    @Test
    public void netty_does_not_invoke_the_next_handlers_write_on_runtime_exception() {
        final ChannelOutboundHandlerAdapter aHandler = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                final String msg1 = "shouldn't get here";
                System.err.println(msg1);
                ctx.write(msg1);
            }
        };

        final EmbeddedChannel channel = new EmbeddedChannel(
                aHandler,
                errorThrowingOutHandler(),
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        // channel.checkException(); // only exceptions that we pass on through ctx.write(err);
        String result = channel.readOutbound();
        assertThat(result).isNull();
    }

    @Test
    public void netty_promises_can_be_used_to_listen_for_next_handlers_success_or_failure() {
        final ChannelOutboundHandlerAdapter handlerWithListener = createErrorHandlingOutboundHandler();

        final EmbeddedChannel channel = new EmbeddedChannel(
                errorThrowingOutHandler(),
                handlerWithListener,
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        String result = channel.readOutbound();
        assertThat(result).isEqualTo("removed offender");
    }

    @Test
    public void netty_promises_can_be_used_to_listen_for_non_immediate_handlers_outcome_if_the_promise_is_passed_on() {
        final ChannelOutboundHandlerAdapter handlerWithListener = createErrorHandlingOutboundHandler();

        final EmbeddedChannel channel = new EmbeddedChannel(
                errorThrowingOutHandler(),
                outboundHandlerPassingOnPromise(),
                handlerWithListener,
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        String result = channel.readOutbound();
        assertThat(result).isEqualTo("removed offender");
    }

    @Test
    public void netty_promises_cannot_be_used_to_listen_for_non_immediate_handlers_outcome_if_a_new_promise_is_passed_on() {
        final ChannelOutboundHandlerAdapter handlerWithListener = createErrorHandlingOutboundHandler();

        final EmbeddedChannel channel = new EmbeddedChannel(
                errorThrowingOutHandler(),
                outboundHandlerWithNewPromiseInWrite(),
                handlerWithListener,
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        String result = channel.readOutbound();
        assertThat(result).isNull();
    }

    @Test
    public void netty_promises_cannot_be_used_to_listen_for_non_immediate_handlers_outcome_if_the_promise_is_not_passed_on() {
        final ChannelOutboundHandlerAdapter handlerWithListener = createErrorHandlingOutboundHandler();

        final EmbeddedChannel channel = new EmbeddedChannel(
                errorThrowingOutHandler(),
                outboundHandlerNotPassingOnPromise(),
                handlerWithListener,
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        String result = channel.readOutbound();
        assertThat(result).isNull();
    }

    @NotNull
    private ChannelOutboundHandlerAdapter createErrorHandlingOutboundHandler() {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ctx.write(msg, promise.addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        // can't get past the handler throwing the exception further up (nearer the head)
                        // so we need to remove it to be able to return a result
                        ctx.pipeline().removeFirst();

                        ctx.writeAndFlush("removed offender");
                    } else {
                        System.err.println("Should never get here");
                    }
                }));
            }
        };
    }


}
