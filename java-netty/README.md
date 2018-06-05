# Netty is a simple, yet complex beast
This is a repository for "learning tests" - tests that investigate how a framework or library works
by writing specific tests. Netty has all kinds of new concepts for programmers coming from blocking
environments and also non-blocking environments using callback-style programming (Node).

These tests are meant to support my learning of Netty

```
mvn test
```


## Exception handling is less than nice
Error handling is almost not touched in "Netty In Practice" (page 94), except at an API level, 
which is a shame, given all the trouble people has. 
With all the asynchronicity, it's very easy for exceptions to be swallowed, making debugging hard.
You can't just have a single point of catching the exceptions you didn't catch explicitly.
You can add error handlers in the handler before the next using promises or futures.

- https://stackoverflow.com/questions/30994095/how-to-catch-all-exception-in-netty
- https://stackoverflow.com/questions/17877611/why-channeloutboundhandler-exceptions-not-caught-by-exceptioncaught-method-n/17879606#17879606
- https://stackoverflow.com/questions/50612403/catch-all-exception-handling-for-outbound-channelhandler<Paste>

## Flushing and writes
- https://github.com/netty/netty/blob/4.1/handler/src/main/java/io/netty/handler/stream/ChunkedWriteHandler.java#L129
- http://normanmaurer.me/presentations/2014-facebook-eng-netty/slides.html#6.0
- https://github.com/netty/netty/issues/6609

- http://www.baeldung.com/testing-netty-embedded-channel
