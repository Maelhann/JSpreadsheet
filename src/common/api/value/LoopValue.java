package common.api.value;

/**
 * A value for tabular cells that are part of a loop.
 */
public final class LoopValue implements Value {

  public static final LoopValue INSTANCE = new LoopValue();

  // had to make loopvalue visible b0ss
  private LoopValue() {
    // not visible (used to be private)
  }

  @Override
  public void evaluate(ValueEvaluator evaluator) {
    evaluator.evaluateLoop();
  }

  @Override
  public String toString() {
    return "#LOOP";
  }
}
