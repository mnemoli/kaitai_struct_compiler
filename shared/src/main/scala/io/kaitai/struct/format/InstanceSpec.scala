package io.kaitai.struct.format

import io.kaitai.struct.datatype.DataType
import io.kaitai.struct.exprlang.{Ast, Expressions}
import io.kaitai.struct.format.AttrSpec.parseContentSpec

sealed abstract class InstanceSpec(val doc: DocSpec) extends YAMLPath {
  def dataTypeComposite: DataType
  def isNullable: Boolean
}
case class ValueInstanceSpec(
  path: List[String],
  private val _doc: DocSpec,
  value: Ast.expr,
  ifExpr: Option[Ast.expr],
  var dataType: Option[DataType]
) extends InstanceSpec(_doc) {
  override def dataTypeComposite = dataType.get
  override def isNullable: Boolean = ifExpr.isDefined
}
case class ParseInstanceSpec(
  id: InstanceIdentifier,
  path: List[String],
  private val _doc: DocSpec,
  dataType: DataType,
  cond: ConditionalSpec,
  pos: Option[Ast.expr],
  io: Option[Ast.expr],
  scanStart: Option[Array[Byte]],
  valid: Option[ValidationSpec]
) extends InstanceSpec(_doc) with AttrLikeSpec {
  override def isLazy = true
}

object InstanceSpec {
  val LEGAL_KEYS_VALUE_INST = Set(
    "value",
    "doc",
    "doc-ref",
    "enum",
    "if"
  )

  def fromYaml(src: Any, path: List[String], metaDef: MetaSpec, id: InstanceIdentifier): InstanceSpec = {
    val srcMap = ParseUtils.asMapStr(src, path)

    ParseUtils.getOptValueExpression(srcMap, "value", path) match {
      case Some(value) =>
        // value instance
        ParseUtils.ensureLegalKeys(srcMap, LEGAL_KEYS_VALUE_INST, path, Some("value instance"))

        // Wrap everything in EnumById if "enum" is used
        val value2 = ParseUtils.getOptValueStr(srcMap, "enum", path) match {
          case None =>
            value
          case Some(enumName) =>
            Ast.expr.EnumById(Ast.identifier(enumName), value)
        }

        val ifExpr = ParseUtils.getOptValueExpression(srcMap, "if", path)

        ValueInstanceSpec(
          path,
          DocSpec.fromYaml(srcMap, path),
          value2,
          ifExpr,
          None
        )
      case None =>
        // normal parse instance
        // TODO: perform proper validation of parse instance keys
        // ParseUtils.ensureLegalKeys(srcMap, LEGAL_KEYS_PARSE_INST, path, Some("parse instance"))

        val pos = ParseUtils.getOptValueExpression(srcMap, "pos", path)
        val io = ParseUtils.getOptValueExpression(srcMap, "io", path)
        val scanStart =  srcMap.get("scan-start").map(parseContentSpec(_, path ++ List("scan-start")))

        val fakeAttrMap = srcMap.filterKeys((key) => key != "pos" && key != "io" && key != "scan-start")
        val a = AttrSpec.fromYaml(fakeAttrMap, path, metaDef, id)
        val valid = srcMap.get("valid").map(ValidationSpec.fromYaml(_, path ++ List("valid")))

        ParseInstanceSpec(id, path, a.doc, a.dataType, a.cond, pos, io, scanStart, valid)
    }
  }
}
