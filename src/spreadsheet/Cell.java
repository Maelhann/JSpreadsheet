package spreadsheet;

import common.api.CellLocation;
import common.api.ExpressionUtils;
import common.api.monitor.Tracker;
import common.api.value.InvalidValue;
import common.api.value.Value;

import java.util.HashSet;
import java.util.Set;

public class Cell implements Tracker<Cell> {
    private CellLocation location;
    private Spreadsheet spreadsheet;
    private Value value;
    private String expression;
    private Set<Cell> isDependentOn;
    private Set<Tracker<Cell>> isADependencyOf;

    Cell(CellLocation newLocation, Spreadsheet newSheet,
         Value newValue, String newExpr) {
        this.location = newLocation;
        this.spreadsheet = newSheet;
        this.value = newValue;
        this.isADependencyOf = new HashSet<>();
        this.isDependentOn = new HashSet<>();
        setExpression(newExpr);

    }

    Cell(CellLocation newLocation, Spreadsheet newSheet) {
        this.location = newLocation;
        this.spreadsheet = newSheet;
        this.value = null;
        this.expression = "";
        this.isDependentOn = new HashSet<>();
        this.isADependencyOf = new HashSet<>();
    }

    public Set<Cell> getIsDependentOn() {
        return this.isDependentOn;
    }

    CellLocation getLocation() {
        return this.location;
    }

    Value getValue() {
        return this.value;
    }

    void setValue(Value value) {
        this.value = value;
    }

    String getExpression() {
        return this.expression;
    }

    void setExpression(String expression) {
        if (isDependentOn.size() != 0) {
            isDependentOn.forEach(c -> c.removeTracker(this));
            isDependentOn = new HashSet<>();
        }
        this.expression = expression;
        this.value = new InvalidValue(expression);
        this.spreadsheet.addRecomputable(this);

        Set<CellLocation> referencedLocations = ExpressionUtils
                .getReferencedLocations(expression);


        referencedLocations.forEach(c -> {
            Cell cell;
            if (!spreadsheet.contains(c)) {
                cell = this.spreadsheet.addCell(c);
            } else {
                cell = this.spreadsheet.getCell(c);
            }
            if (!isDependentOn.contains(c)) {
                isDependentOn.add(cell);
            }
            cell.isADependencyOf.add(this);
        });

        this.spreadsheet.recompute();
        isADependencyOf.forEach(c -> c.update(this));


    }


    public void update(Cell changed) {
        if (!spreadsheet.isRecomputable(this)) {
            spreadsheet.addRecomputable(this);
            setValue(new InvalidValue(getExpression()));
            this.isADependencyOf.forEach(c -> c.update(this));
        }
    }


    private void removeTracker(Tracker<Cell> tracker) {
        isADependencyOf.remove(tracker);
    }


}

