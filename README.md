# scalajs-transport

Scala/Scala.js library for simple, unified communication between Scala systems. The idea is to have a single interface at the core of the library, [Transport](transport/core/shared/src/main/scala/transport/Transport.scala), which allows to write generic network applications. Depending on the platform, one can then use any of the available implementations of this interface:

Platform       | WebSocket | [SockJS](https://github.com/sockjs/) | WebRTC
---------------|-----------|--------|--------
JavaScript     | client    | client | client
Play Framework | server    | server | -
Netty          | both      | [*incomming*](https://github.com/netty/netty/pull/1615) | -
Tyrus          | client    | -      | -

In addition, the library provides wrappers for [Autowire](https://github.com/lihaoyi/autowire) and [Akka](http://akka.io/)/[scala-js-actors](https://github.com/sjrd/scala-js-actors) on top of the [Transport](transport/core/shared/src/main/scala/transport/Transport.scala) interface for higher level of abstraction. More information can be found on [this report](https://github.com/OlivierBlanvillain/master-thesis), chapter 2 is about the library.

## Setup

To avoid unnecessary dependencies, the library it split on 8 artifacts, pick only what you need if you don't want to download Play, Akka and Netty. It's on Sonatype Snapshots, compiled for Scala 2.11 and Scala.js 0.6.0+:

```scala
resolvers +=
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.github.olivierblanvillain" %%% "transport-core"       % "0.1-SNAPSHOT",
  "com.github.olivierblanvillain" %%% "transport-javascript" % "0.1-SNAPSHOT",
  "com.github.olivierblanvillain" %%% "transport-webrtc"     % "0.1-SNAPSHOT",
  "com.github.olivierblanvillain" %%% "transport-netty"      % "0.1-SNAPSHOT",
  "com.github.olivierblanvillain" %%% "transport-play"       % "0.1-SNAPSHOT",
  "com.github.olivierblanvillain" %%% "transport-tyrus"      % "0.1-SNAPSHOT",
  "com.github.olivierblanvillain" %%% "transport-rpc"        % "0.1-SNAPSHOT",
  "com.github.olivierblanvillain" %%% "transport-akka"       % "0.1-SNAPSHOT"
)
```

## What's in the box

- Core:
    - [transport.Transport](transport/core/shared/src/main/scala/transport/Transport.scala) (js+jvm)
    - [transport.ConnectionUtils](transport/core/shared/src/main/scala/transport/ConnectionUtils.scala) (js+jvm)
- JavaScript:
    - [transport.javascript.SockJSClient](transport/javascript/js/src/main/scala/transport/javascript/SockJSClient.scala) (js)
    - [transport.javascript.WebSocketClient](transport/javascript/js/src/main/scala/transport/javascript/WebSocketClient.scala) (js)
- WebRTC:
    - [transport.webrtc.WebRTCClient](transport/webrtc/js/src/main/scala/transport/webrtc/WebRTCClient.scala) (js)
    - [transport.webrtc.WebRTCClientFallback](transport/webrtc/shared/src/main/scala/transport/webrtc/WebRTCClientFallback.scala) (js+jvm)
- Netty:
    - [transport.netty.WebSocketServer](transport/netty/jvm/src/main/scala/transport/netty/WebSocketServer.scala) (jvm)
- Play:
    - [transport.play.WebSocketServer](transport/play/jvm/src/main/scala/transport/play/WebSocketServer.scala) (jvm)
    - [transport.play.PlayUtils](transport/play/jvm/src/main/scala/transport/play/PlayUtils.scala) (jvm)
    - [transport.play.PlayUtils](transport/play/js/src/main/scala/transport/play/PlayUtils.scala) (js)
- Tyrus:
    - [transport.tyrus.WebSocketClient](transport/tyrus/jvm/src/main/scala/transport/tyrus/WebSocketClient.scala) (jvm)
- RPC:
    - [transport.rpc.RpcWrapper](transport/rpc/src/main/scala/transport/rpc/RpcWrapper.scala) (js+jvm)
- Akka:
    - [transport.akka.ActorWrapper](transport/akka/shared/src/main/scala/transport/akka/ActorWrapper.scala) (js+jvm)

To use the [ActorWrapper](transport/akka/shared/src/main/scala/transport/akka/ActorWrapper.scala) on JavaScript you will to `publish-local` [scala-js-pickling](https://github.com/OlivierBlanvillain/scala-js-pickling/tree/upgrade-0.6.0) and [scala-js-actors](https://github.com/OlivierBlanvillain/scala-js-actors/tree/wip-upgrades) for Scala.js 0.6.0.
