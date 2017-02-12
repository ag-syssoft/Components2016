package comp.solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.impl.FixedIntVarImpl;

import static java.lang.Math.sqrt;

/**
 * Searches for Sudokuresults in a Sudokufield.
 * Returns 0 if no solutions exist, 1 if exactly one exists, or 2 if more then one exist.
 */
public class SudokuSolver {
    private int[] field;
    private int size;
    private int boxsize;

    /**
     * @param field has to be of squarenumber*squarenumber length, else #IllegalArgumentException will be thrown. Free values = 0;
     */
    SudokuSolver(int[] field) {
        this.field = field;
        this.size = (int) sqrt(field.length);
        if (size * size != field.length) {
            throw new IllegalArgumentException(size + "length not a square number " + field.length);
        }
        this.boxsize = (int) sqrt(size);
        if (boxsize * boxsize != size) {
            throw new IllegalArgumentException(boxsize + " size not a square number " + size);
        }
        //System.out.println("SudokuSolver of size " + size + " created");
    }

    /**
     * Searches for Sudokuresults in the given sudokufield.
     *
     * @return 0 if no solutions exist, 1 if exactly one exists, or 2 if more then one exist.
     */
    int search() {
        Model model = new Model();
        IntVar[][] horizontalVars = new IntVar[size][size];
        IntVar[][] verticalVars = new IntVar[size][size];
        IntVar[][] boxVars = new IntVar[size][size];
        for (int vertical = 0; vertical < size; vertical++) {
            int boxVerticalV = vertical % boxsize;
            int boxVertical = vertical / 3;
            for (int horizontal = 0; horizontal < size; horizontal++) {
                int value = field[horizontal + (vertical * 9)];
                IntVar v;
                if (value > 0) {
                    if (value > size) {
                        throw new IllegalArgumentException(vertical + "/" + horizontal + " value > size: " + value);
                    } else {
                        v = new FixedIntVarImpl(vertical + "/" + horizontal, value, model);
                    }
                } else {
                    v = model.intVar(vertical + "/" + horizontal, 1, size, true);
                }
                horizontalVars[horizontal][vertical] = v;
                verticalVars[vertical][horizontal] = v;
                int j = horizontal % boxsize;
                //System.out.println(boxVertical + " h " + (horizontal / 3) * 3 + " = " + (boxVertical + ((horizontal / 3) * 3)) + " / " + (boxVerticalV + (j * 3))); //TODO REMOVE DEBUG ONLY
                boxVars[boxVertical + ((horizontal / 3) * 3)][boxVerticalV + (j * 3)] = v;
            }
        }

        for (int i = 0; i < size; i++) {
            model.allDifferent(verticalVars[i]).post();
            model.allDifferent(horizontalVars[i]).post();
            model.allDifferent(boxVars[i]).post();
        }
        Solver solver = model.getSolver();
        int solutions = 0;
        while (solver.solve()) {
            if (++solutions > 1) {
                return solutions;
            }
        }
        return solutions;
    }
}