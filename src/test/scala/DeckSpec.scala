import collection.mutable.Stack
import org.scalatest.flatspec.AnyFlatSpec

class DeckSpec extends AnyFlatSpec {
  "A deck" should "shuffle cards" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    assert(stack.pop() === 2)
    assert(stack.pop() === 1)
  }
}
