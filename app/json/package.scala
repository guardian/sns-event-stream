import play.api.libs.json._

package object json {
  def requireField(key: String, value: JsValue) = new Reads[Unit] {
    override def reads(json: JsValue): JsResult[Unit] = json match {
      case JsObject(fields) if fields.contains(key -> value) => JsSuccess(())
      case _ => JsError(s"Missing required field $key -> $value")
    }
  }
}
