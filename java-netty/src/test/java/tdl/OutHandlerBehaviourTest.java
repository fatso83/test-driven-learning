package tdl;

import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class OutHandlerBehaviourTest {

    private static ChannelOutboundHandlerAdapter outboundHandlerNotPassingOnPromise() {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ctx.write(msg); // will implicitly create a new promise!
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

    private static ChannelOutboundHandlerAdapter outboundHandlerPassingOnPromiseWithWriteListener(ChannelFutureListener listener) {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ctx.write(msg, promise).addListener(listener);
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
    private static String prefixOk(Object msg) {
        return "OK:" + msg;
    }

    @NotNull
    private static ChannelOutboundHandlerAdapter createOutboundHandlerWithErrorHandlingOnPromiseListener() {
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


    private static ChannelOutboundHandlerAdapter outboundHandlerNotPassingOnPromiseWithWriteListener(ChannelFutureListener listener) {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ctx.write(msg).addListener(listener);
            }
        };
    }


    @Test
    public void netty_invokes_the_next_handlers_write_with_its_written_message() {
        final ChannelOutboundHandlerAdapter aHandler = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ctx.write(prefixOk(msg));
            }
        };

        final EmbeddedChannel channel = new EmbeddedChannel(
                aHandler,
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        channel.flushOutbound();
        String result = channel.readOutbound();
        assertThat(result).isEqualTo(prefixOk("hello"));
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
        final ChannelOutboundHandlerAdapter handlerWithListener = createOutboundHandlerWithErrorHandlingOnPromiseListener();

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
        final ChannelOutboundHandlerAdapter handlerWithListener = createOutboundHandlerWithErrorHandlingOnPromiseListener();

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
        final ChannelOutboundHandlerAdapter handlerWithListener = createOutboundHandlerWithErrorHandlingOnPromiseListener();

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

        final EmbeddedChannel channel = new EmbeddedChannel(
                errorThrowingOutHandler(),
                outboundHandlerNotPassingOnPromise(),
                createOutboundHandlerWithErrorHandlingOnPromiseListener(),
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        String result = channel.readOutbound();
        assertThat(result).isNull();
    }

  @Test
    public void an_error_handler_near_the_tail_cannot_catch_an_error_from_a_handler_nearer_the_head() {

        final EmbeddedChannel channel = new EmbeddedChannel(
                errorThrowingOutHandler(),
                mirrorInHandler(),
                createOutboundHandlerWithErrorHandlingOnPromiseListener()
        );

        channel.writeInbound("hello");
        String result = channel.readOutbound();
        assertThat(result).isNull();
    }


    @Test
    public void netty_invokes_a_write_listener_once_on_a_normal_write() {
        AtomicInteger i = new AtomicInteger(0);

        final ChannelFutureListener channelFutureListener = future -> i.addAndGet(1);

        final EmbeddedChannel channel = new EmbeddedChannel(
                outboundHandlerNotPassingOnPromiseWithWriteListener(channelFutureListener),
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        channel.flushOutbound();
        assertThat(i.get()).isEqualTo(1);
    }


    @Test
    public void netty_invokes_a_write_listener_once_on_an_erronous_write() {
        AtomicInteger i = new AtomicInteger(0);

        final ChannelFutureListener channelFutureListener = future -> i.addAndGet(1);

        final EmbeddedChannel channel = new EmbeddedChannel(
                errorThrowingOutHandler(),
                outboundHandlerNotPassingOnPromiseWithWriteListener(channelFutureListener),
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        channel.flushOutbound();
        assertThat(i.get()).isEqualTo(1);
    }


    @Test
    public void a_write_listener_can_remove_listeners_from_the_pipeline_and_write_on_an_erronous_write() {

        final EmbeddedChannel channel = new EmbeddedChannel(
                errorThrowingOutHandler(),
                new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                        ctx.write(msg).addListener(future -> {
                            if (!future.isSuccess()) {
                                ctx.pipeline().removeFirst();
                                ctx.write("handled the error");
                            }
                        });
                    }
                },
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        channel.flushOutbound();
        final String s = channel.readOutbound();
        assertThat(s).isEqualTo("handled the error");
    }

    @Test
    public void only_the_listener_added_to_the_final_write_is_invoked_on_success_if_the_promise_is_not_passed_on() {
        CompletableFuture<Boolean> listenerAOp = new CompletableFuture<>();
        CompletableFuture<Boolean> listenerBOp = new CompletableFuture<>();

        final EmbeddedChannel channel = new EmbeddedChannel(
                outboundHandlerNotPassingOnPromiseWithWriteListener(future -> listenerAOp.complete(true)),
                outboundHandlerNotPassingOnPromiseWithWriteListener(future -> listenerBOp.complete(true)),
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        channel.flushOutbound();
        assertTrue(listenerAOp.isDone());
        assertFalse(listenerBOp.isDone());
    }


    @Test
    public void only_the_listener_added_to_the_final_write_is_invoked_on_error_if_the_promise_is_not_passed_on() throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> listenerAOp = new CompletableFuture<>();
        CompletableFuture<Boolean> listenerBOp = new CompletableFuture<>();

        final EmbeddedChannel channel = new EmbeddedChannel(
                errorThrowingOutHandler(),
                outboundHandlerNotPassingOnPromiseWithWriteListener(future -> listenerAOp.complete(!future.isSuccess() && future.isDone())),
                outboundHandlerNotPassingOnPromiseWithWriteListener(future -> listenerBOp.complete(true)),
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        channel.flushOutbound();
        assertTrue(listenerAOp.isDone());
        assertFalse(listenerBOp.isDone());
        // sanity check
        boolean writeDidFail = listenerAOp.get();
        assertTrue(writeDidFail);
    }


    @Test
    public void all_listeners_are_invoked_on_success_if_the_same_promise_is_passed_on() {
        CompletableFuture<Boolean> listenerAOp = new CompletableFuture<>();
        CompletableFuture<Boolean> listenerBOp = new CompletableFuture<>();

        final EmbeddedChannel channel = new EmbeddedChannel(
                outboundHandlerPassingOnPromiseWithWriteListener(future -> {
                    System.out.println('A');
                    listenerAOp.complete(true);
                }),
                outboundHandlerPassingOnPromiseWithWriteListener(future -> {
                    System.out.println('B');
                    listenerBOp.complete(true);
                }),
                mirrorInHandler()
        );

        channel.writeInbound("hello");
        channel.flushOutbound();
        assertTrue(listenerAOp.isDone());
        assertTrue(listenerBOp.isDone());
    }

    @Test
    public void write_listeners_are_invoked_from_the_tail() {
        final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

        final EmbeddedChannel channel = new EmbeddedChannel(
                outboundHandlerPassingOnPromiseWithWriteListener(future -> {
                    queue.add("LAST");
                }),
                outboundHandlerPassingOnPromiseWithWriteListener(future -> {
                    queue.add("FIRST");
                }),
                mirrorInHandler()
        );

        channel.writeInbound("_ignored");
        channel.flushOutbound();
        assertThat(queue.poll()).isEqualTo("LAST");
        assertThat(queue.poll()).isEqualTo("FIRST");
    }
    @Test
    public void a_listener_can_write_to_a_channel_after_the_parent_write_has_completed() {

        final EmbeddedChannel channel = new EmbeddedChannel(
                new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                        ctx.write(msg).addListener(future -> {
                            if(future.isDone()) {
                                ctx.write("Write in listener A");
                            }
                        });
                    }
                },
                mirrorInHandler()
        );

        channel.writeInbound("hello");

        final String originalWrite = channel.flushOutbound().readOutbound();
        assertThat(originalWrite).isEqualTo("hello");
        final String listenerWrite = channel.flushOutbound().readOutbound();
        assertThat(listenerWrite).isEqualTo("Write in listener A");
    }


    @Test
    @Ignore
    public void template() {

        final EmbeddedChannel channel = new EmbeddedChannel(
                new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                        ctx.write(msg).addListener(future -> {
                            final Channel channel1 = ctx.channel();
                            System.out.println(format("Listener in Handler A, %s, %s, %s, %s", future.isDone(), channel1.isWritable(), channel1.isActive(), channel1.isOpen()));
                            ctx.write("Write in listener A, ");
//                            if(!future.isSuccess()) {
//                                ctx.pipeline().removeFirst();
//                                ctx.write("A handled the error");
//                            }
                        });
                    }
                },
                new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                        System.out.println("Handler B");
                        ctx.write(msg).addListener(future -> {
                            System.out.println("Listener in Handler B");
                            ctx.write("Write in listener B");
                        });
                    }
                },

                mirrorInHandler()
        );

        channel.writeInbound("hello");
        channel.flushOutbound();
        final String s = channel.readOutbound();
//        assertThat(s).isEqualTo("handled the error");
        System.out.println(channel.flushOutbound().readOutbound().toString());
    }
}
