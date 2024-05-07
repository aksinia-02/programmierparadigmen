package aufgabe8;

import java.util.Random;

// Note that java's random is inherently not functional and relies on mutation.
// This does not cause a problem with aliasing since the values are random regardless.
public record Parameters(double q0, double alpha, double beta, double rho, double tao0, Random random) {

}
