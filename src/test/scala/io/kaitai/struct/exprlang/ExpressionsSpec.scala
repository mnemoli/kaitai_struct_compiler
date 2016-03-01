package io.kaitai.struct.exprlang

import io.kaitai.struct.exprlang.Ast.expr._
import io.kaitai.struct.exprlang.Ast.operator._
import io.kaitai.struct.exprlang.Ast.cmpop._
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class ExpressionsSpec extends FunSpec {
  describe("Expressions.parse") {
    it("parses single positive integer") {
      Expressions.parse("123") should be (Num(123))
    }

    it("parses single negative integer") {
      Expressions.parse("-456") should be (Num(-456))
    }

    it("parses 1 + 2") {
      Expressions.parse("1 + 2") should be (BinOp(Num(1), Add, Num(2)))
    }

    it("parses 1 + 2 + 5") {
      Expressions.parse("1 + 2 + 5") should be (
        BinOp(BinOp(Num(1), Add, Num(2)), Add, Num(5))
      )
    }

    it("parses (1 + 2) / (7 * 8)") {
      Expressions.parse("(1 + 2) / (7 * 8)") should be (
        BinOp(
          BinOp(Num(1), Add, Num(2)),
          Div,
          BinOp(Num(7), Mult, Num(8))
        )
      )
    }

    it("parses 1 < 2") {
      Expressions.parse("1 < 2") should be (Compare(Num(1), Lt, Num(2)))
    }
  }
}
