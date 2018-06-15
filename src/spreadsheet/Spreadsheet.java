package spreadsheet;

import common.api.CellLocation;
import common.api.ExpressionUtils;
import common.api.Tabular;
import common.api.value.LoopValue;
import common.api.value.StringValue;
import common.api.value.Value;
import common.api.value.ValueEvaluator;

import java.util.*;

public class Spreadsheet implements Tabular {
    private Map<CellLocation, Cell> cells;
    private Deque<Cell> reComputables;
    private Map<CellLocation, Double> locationToVal = new HashMap<>();


    Spreadsheet() {
        this.cells = new HashMap<>();
        this.reComputables = new ArrayDeque<>();
    }

    @Override
    public void setExpression(CellLocation location, String expression) {
        Cell cell;
        if (cells.containsKey(location)) {
            cell = cells.get(location);
            cell.setExpression(expression);
            cell.setValue(new StringValue(expression));
        } else {
            cell = new Cell(location, this,
                    new StringValue(expression), expression);
            cells.put(location, cell);
        }
    }

    @Override
    public void recompute() {
        for (int i = 0; i < reComputables.size(); i++) {
            if (reComputables.iterator().hasNext()) {
                recomputeCell(reComputables.iterator().next());
            }
        }

        reComputables.clear();
    }

    private void calculateCellValue(Cell cell) {

        ValueEvaluator evaluator = new ValueEvaluator() {
            @Override
            public void evaluateDouble(double value) {
                locationToVal.put(cell.getLocation(), value);
            }

            @Override
            public void evaluateLoop() {

            }

            @Override
            public void evaluateString(String expression) {

            }

            @Override
            public void evaluateInvalid(String expression) {

            }
        };


        Value value = ExpressionUtils.computeValue(cell.getExpression(), locationToVal);

        value.evaluate(evaluator);

        cell.setValue(value);


    }


    private void recomputeCell(Cell c) {
        c.setValue(new StringValue(c.getExpression()));
        checkLoops(c, new LinkedHashSet<>());
        if (!c.getValue().equals(LoopValue.INSTANCE)) {
            reComputables.remove(c);
            for (Cell dep : c.getIsDependentOn()) {
                if (isRecomputable(dep)) {
                    reComputables.addFirst(dep);
                }
                recomputeCell(dep);
            }
            reComputables.addLast(c);
            calculateCellValue(c);
            reComputables.remove(c);
            recompute();
        }
    }

    private void checkLoops(Cell c, LinkedHashSet<Cell> cellseen) {
        c.getIsDependentOn().remove(c);
        if (cellseen.contains(c)) {
            markAsValidatedLoop(c, cellseen);
        } else {
            cellseen.add(c);
            c.getIsDependentOn().forEach(ref -> checkLoops(ref, cellseen));
            cellseen.remove(c);
        }
    }

    private void markAsValidatedLoop(Cell startCell, LinkedHashSet<Cell> cells) {
        startCell.setValue(LoopValue.INSTANCE);
        cells.forEach(c -> {
            c.setValue(LoopValue.INSTANCE);
            if (reComputables.contains(c)) {
                reComputables.remove(c);
            }
        });
    }

    public boolean isRecomputable(Cell cell) {
        return this.reComputables.contains(cell);
    }

    public boolean contains(CellLocation location) {
        return cells.containsKey(location);
    }

    public Cell getCell(CellLocation location) {
        return cells.get(location);
    }

    @Override
    public String getExpression(CellLocation location) {
        if (cells.containsKey(location)) {
            return cells.get(location).getExpression();
        } else {
            return "";
        }
    }

    @Override
    public Value getValue(CellLocation location) {
        if (cells.containsKey(location)) {
            return cells.get(location).getValue();
        } else {
            return null;
        }

    }

    Cell addCell(CellLocation location) {
        Cell cell = new Cell(location, this);
        cell.setValue(getValue(location));
        cell.setExpression(getExpression(location)); // problem ici
        cells.put(location, cell);
        return cell;
    }

    void addRecomputable(Cell cell) {
        if (!isRecomputable(cell)) {
            reComputables.add(cell);
        }
    }


}
