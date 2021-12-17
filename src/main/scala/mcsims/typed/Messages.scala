package mcsims.typed

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object Messages {

  sealed trait ClientMessages

  final case class FromClient(command: String) extends ClientMessages

  implicit val clientMessageDecoder: Decoder[FromClient] = deriveDecoder
  implicit val clientMessageEncoder: Encoder[FromClient] = deriveEncoder

  sealed trait ServerMessages

  final case class SampleResponse(title: String, message: String) extends ServerMessages
  final case class ErrorMessage(message: String) extends ServerMessages

  implicit val sampleResponseDecoder: Decoder[SampleResponse] = deriveDecoder
  implicit val sampleResponseEncoder: Encoder[SampleResponse] = deriveEncoder

  implicit val serverErrorDecoder: Decoder[ErrorMessage] = deriveDecoder
  implicit val serverErrorEncoder: Encoder[ErrorMessage] = deriveEncoder
}
