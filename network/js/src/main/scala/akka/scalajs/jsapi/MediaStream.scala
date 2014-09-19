package akka.scalajs.jsapi

import scala.scalajs.js

class MediaStreamConstraints extends js.Object {
  val audio: js.Any = ???
  val video: js.Any = ???
}

object MediaStreamConstraints extends js.Object {
}

class MediaTrackConstraints extends js.Object {
  val mandatory: MediaTrackConstraintSet = ???
  val optional: js.Array[MediaTrackConstraint] = ???
}

object MediaTrackConstraints extends js.Object {
}

class MediaTrackConstraintSet extends js.Object {
}

object MediaTrackConstraintSet extends js.Object {
}

trait MediaTrackConstraint extends js.Object {
}

object MediaTrackConstraint extends js.Object {
/* ??? ConstructorMember(FunSignature(List(),List(),Some(TypeRef(TypeName(MediaTrackConstraints),List())))) */
}

trait Navigator extends js.Object {
  def getUserMedia(constraints: MediaStreamConstraints, successCallback: js.Function1[js.Any, Unit], errorCallback: js.Function1[Error, Unit]): Unit = ???
  def webkitGetUserMedia(constraints: MediaStreamConstraints, successCallback: js.Function1[js.Any, Unit], errorCallback: js.Function1[Error, Unit]): Unit = ???
}

trait EventHandler extends js.Object {
  def apply(event: Event): Unit = ???
}

trait NavigatorUserMediaSuccessCallback extends js.Object {
  def apply(stream: LocalMediaStream): Unit = ???
}

class NavigatorUserMediaError extends js.Object {
  val PERMISSION_DENIED: js.Number = ???
  val code: js.Number = ???
}

object NavigatorUserMediaError extends js.Object {
  val PERMISSION_DENIED: js.Number = ???
}

trait NavigatorUserMediaErrorCallback extends js.Object {
  def apply(error: NavigatorUserMediaError): Unit = ???
}

class MediaStreamTrackList extends js.Object {
  val length: js.Number = ???
  val item: MediaStreamTrack = ???
  def add(track: MediaStreamTrack): Unit = ???
  def remove(track: MediaStreamTrack): Unit = ???
  val onaddtrack: js.Function1[Event, Unit] = ???
  val onremovetrack: js.Function1[Event, Unit] = ???
}

object MediaStreamTrackList extends js.Object {
}

object webkitMediaStreamTrackList extends js.Object {
/* ??? ConstructorMember(FunSignature(List(),List(),Some(TypeRef(TypeName(MediaStreamTrackList),List())))) */
}

class MediaStream extends js.Object {
  def this(trackContainers: js.Array[MediaStream]) = this()
  val label: js.String = ???
  val id: js.String = ???
  def getAudioTracks(): MediaStreamTrackList = ???
  def getVideoTracks(): MediaStreamTrackList = ???
  val ended: js.Boolean = ???
  val onended: js.Function1[Event, Unit] = ???
}

object MediaStream extends js.Object {
}

object webkitMediaStream extends js.Object {
/* ??? ConstructorMember(FunSignature(List(),List(),Some(TypeRef(TypeName(MediaStream),List())))) */
/* ??? ConstructorMember(FunSignature(List(),List(FunParam(Ident(trackContainers),false,Some(TypeRef(TypeName(Array),List(TypeRef(TypeName(MediaStream),List())))))),Some(TypeRef(TypeName(MediaStream),List())))) */
/* ??? ConstructorMember(FunSignature(List(),List(FunParam(Ident(trackContainers),false,Some(TypeRef(TypeName(Array),List(TypeRef(TypeName(MediaStreamTrackList),List())))))),Some(TypeRef(TypeName(MediaStream),List())))) */
/* ??? ConstructorMember(FunSignature(List(),List(FunParam(Ident(trackContainers),false,Some(TypeRef(TypeName(Array),List(TypeRef(TypeName(MediaStreamTrack),List())))))),Some(TypeRef(TypeName(MediaStream),List())))) */
}

trait SourceInfo extends js.Object {
  val label: js.String = ???
  val id: js.String = ???
  val kind: js.String = ???
  val facing: js.String = ???
}

object SourceInfo extends js.Object {
}

trait LocalMediaStream extends MediaStream {
  def stop(): Unit = ???
}

class MediaStreamTrack extends js.Object {
  val kind: js.String = ???
  val label: js.String = ???
  val enabled: js.Boolean = ???
  val LIVE: js.Number = ???
  val MUTED: js.Number = ???
  val ENDED: js.Number = ???
  val readyState: js.Number = ???
  val onmute: js.Function1[Event, Unit] = ???
  val onunmute: js.Function1[Event, Unit] = ???
  val onended: js.Function1[Event, Unit] = ???
}

object MediaStreamTrack extends js.Object {
  val LIVE: js.Number = ???
  val MUTED: js.Number = ???
  val ENDED: js.Number = ???
  val getSources: js.Function1[js.Function1[js.Array[SourceInfo], Unit], Unit] = ???
}

trait streamURL extends js.Object {
  def createObjectURL(stream: MediaStream): js.String = ???
}

trait WebkitURL extends streamURL {
}

object webkitURL extends js.Object { // TODO: extends URL
/* ??? ConstructorMember(FunSignature(List(),List(),Some(TypeRef(TypeName(streamURL),List())))) */
  def createObjectURL(stream: MediaStream): js.String = ???
}
