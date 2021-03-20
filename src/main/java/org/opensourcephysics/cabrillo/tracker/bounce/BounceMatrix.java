/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */

package org.opensourcephysics.cabrillo.tracker.bounce;


import lombok.Getter;
import lombok.Setter;

/**
 * A subset of methods from the Jama Matrix class used for the BounceModel.
 * This incorporates the LUDecomposition and QRDecomposition classes as static
 * inner classes.
 * Almost all javadoc and other comments have been removed for compactness.
 * <p>
 * The entire JAMA matrix package including full documentation is available from
 * http://math.nist.gov/javanumerics/jama
 *
 * @author The MathWorks, Inc. and the National Institute of Standards and Technology.
 * @author Doug Brown (this file)
 * @version 5 August 1998 (Jama), 12 Jan 2012 (this file)
 */
@Getter
@Setter
public class BounceMatrix {

    public BounceMatrix(int rowDimension, int columnDimension) {
        this.rowDimension = rowDimension;
        this.columnDimension = columnDimension;
        array = new double[rowDimension][columnDimension];
    }

    public BounceMatrix(double[][] array, int rowDimension, int columnDimension) {
        this.array = array;
        this.rowDimension = rowDimension;
        this.columnDimension = columnDimension;
    }

    public BounceMatrix(double[][] array) {
        rowDimension = array.length;
        columnDimension = array[0].length;
        for (int i = 0; i < rowDimension; i++) {
            if (array[i].length != columnDimension) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
        }
        this.array = array;
    }


    private double[][] array; // a

    private int rowDimension; // m
    private int columnDimension; // n

    public double[][] getArrayCopy() {
        double[][] arrayCopy = new double[rowDimension][columnDimension];
        for (int i = 0; i < rowDimension; i++) {
            if (columnDimension >= 0){
                System.arraycopy(array[i], 0, arrayCopy[i], 0, columnDimension);
            }
        }
        return arrayCopy;
    }

    public BounceMatrix getMatrix(int i0, int i1, int j0, int j1) {
        BounceMatrix X = new BounceMatrix(i1 - i0 + 1, j1 - j0 + 1);
        double[][] B = X.getArray();
        try {
            for (int i = i0; i <= i1; i++) {
                if (j1 + 1 - j0 >= 0) System.arraycopy(array[i], j0, B[i - i0], 0, j1 + 1 - j0);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("SubMatrix indices");
        }
        return X;
    }

    public BounceMatrix getMatrix(int[] r, int j0, int j1) {
        BounceMatrix X = new BounceMatrix(r.length, j1 - j0 + 1);
        double[][] B = X.getArray();
        try {
            for (int i = 0; i < r.length; i++) {
                if (j1 + 1 - j0 >= 0) System.arraycopy(array[r[i]], j0, B[i], 0, j1 + 1 - j0);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("SubMatrix indices"); //$NON-NLS-1$
        }
        return X;
    }

    public BounceMatrix minus(BounceMatrix B) {
        if (B.rowDimension != rowDimension || B.columnDimension != columnDimension) {
            throw new IllegalArgumentException("Matrix dimensions must agree!"); //$NON-NLS-1$
        }
        BounceMatrix X = new BounceMatrix(rowDimension, columnDimension);
        double[][] C = X.getArray();
        for (int i = 0; i < rowDimension; i++) {
            for (int j = 0; j < columnDimension; j++) {
                C[i][j] = array[i][j] - B.array[i][j];
            }
        }
        return X;
    }

    public BounceMatrix times(BounceMatrix B) {
        if (B.rowDimension != columnDimension) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree!");
        }
        BounceMatrix X = new BounceMatrix(rowDimension, B.columnDimension);
        double[][] C = X.getArray();
        double[] Bcolj = new double[columnDimension];
        for (int j = 0; j < B.columnDimension; j++) {
            for (int k = 0; k < columnDimension; k++) {
                Bcolj[k] = B.array[k][j];
            }
            for (int i = 0; i < rowDimension; i++) {
                double[] Arowi = array[i];
                double s = 0;
                for (int k = 0; k < columnDimension; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }
        return X;
    }

    public BounceMatrix solve(BounceMatrix B) {
        return (rowDimension == columnDimension ? (new LUDecomposition(this)).solve(B) :
                (new QRDecomposition(this)).solve(B));
    }

    public BounceMatrix inverse() {
        return solve(identity(rowDimension, rowDimension));
    }

    public static BounceMatrix identity(int m, int n) {
        BounceMatrix A = new BounceMatrix(m, n);
        double[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = (i == j ? 1.0 : 0.0);
            }
        }
        return A;
    }

//_______________________ LUDecomposition class __________________________

    static class LUDecomposition {

        private final double[][] LU;
        private final int m;
        private final int n;
        private final int[] piv;

        public LUDecomposition(BounceMatrix A) {

            LU = A.getArrayCopy();
            m = A.getRowDimension();
            n = A.getColumnDimension();
            piv = new int[m];
            for (int i = 0; i < m; i++) {
                piv[i] = i;
            }
            int pivSign = 1;
            double[] LUrowi;
            double[] LUcolj = new double[m];

            for (int j = 0; j < n; j++) {

                for (int i = 0; i < m; i++) {
                    LUcolj[i] = LU[i][j];
                }

                for (int i = 0; i < m; i++) {
                    LUrowi = LU[i];
                    int kmax = Math.min(i, j);
                    double s = 0.0;
                    for (int k = 0; k < kmax; k++) {
                        s += LUrowi[k] * LUcolj[k];
                    }
                    LUrowi[j] = LUcolj[i] -= s;
                }

                int p = j;
                for (int i = j + 1; i < m; i++) {
                    if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                        p = i;
                    }
                }
                if (p != j) {
                    for (int k = 0; k < n; k++) {
                        double t = LU[p][k];
                        LU[p][k] = LU[j][k];
                        LU[j][k] = t;
                    }
                    int k = piv[p];
                    piv[p] = piv[j];
                    piv[j] = k;
                    pivSign = -pivSign;
                }

                if (j < m & LU[j][j] != 0.0) {
                    for (int i = j + 1; i < m; i++) {
                        LU[i][j] /= LU[j][j];
                    }
                }
            }
        }

        public boolean isNonsingular() {
            for (int j = 0; j < n; j++) {
                if (LU[j][j] == 0)
                    return false;
            }
            return true;
        }

        public BounceMatrix solve(BounceMatrix B) {
            if (B.getRowDimension() != m) {
                throw new IllegalArgumentException("Matrix row dimensions must agree!"); //$NON-NLS-1$
            }
            if (!this.isNonsingular()) {
                throw new RuntimeException("Matrix is singular!"); //$NON-NLS-1$
            }
            // Copy right hand side with pivoting
            int nx = B.getColumnDimension();
            BounceMatrix Xmat = B.getMatrix(piv, 0, nx - 1);
            double[][] X = Xmat.getArray();

            // Solve L*Y = B(piv,:)
            for (int k = 0; k < n; k++) {
                for (int i = k + 1; i < n; i++) {
                    for (int j = 0; j < nx; j++) {
                        X[i][j] -= X[k][j] * LU[i][k];
                    }
                }
            }
            // Solve U*X = Y;
            for (int k = n - 1; k >= 0; k--) {
                for (int j = 0; j < nx; j++) {
                    X[k][j] /= LU[k][k];
                }
                for (int i = 0; i < k; i++) {
                    for (int j = 0; j < nx; j++) {
                        X[i][j] -= X[k][j] * LU[i][k];
                    }
                }
            }
            return Xmat;
        }
    }

//_________________________ QRDecomposition class __________________________  

    static class QRDecomposition {

        private final double[][] QR;
        private final int m;
        private final int n;
        private final double[] Rdiag;

        public QRDecomposition(BounceMatrix A) {
            QR = A.getArrayCopy();
            m = A.getRowDimension();
            n = A.getColumnDimension();
            Rdiag = new double[n];

            for (int k = 0; k < n; k++) {
                // Compute 2-norm of k-th column without under/overflow.
                double nrm = 0;
                for (int i = k; i < m; i++) {
                    nrm = hypot(nrm, QR[i][k]);
                }

                if (nrm != 0.0) {
                    // Form k-th Householder vector.
                    if (QR[k][k] < 0) {
                        nrm = -nrm;
                    }
                    for (int i = k; i < m; i++) {
                        QR[i][k] /= nrm;
                    }
                    QR[k][k] += 1.0;

                    // Apply transformation to remaining columns.
                    for (int j = k + 1; j < n; j++) {
                        double s = 0.0;
                        for (int i = k; i < m; i++) {
                            s += QR[i][k] * QR[i][j];
                        }
                        s = -s / QR[k][k];
                        for (int i = k; i < m; i++) {
                            QR[i][j] += s * QR[i][k];
                        }
                    }
                }
                Rdiag[k] = -nrm;
            }
        }

        public boolean isFullRank() {
            for (int j = 0; j < n; j++) {
                if (Rdiag[j] == 0)
                    return false;
            }
            return true;
        }

        public BounceMatrix solve(BounceMatrix B) {
            if (B.getRowDimension() != m) {
                throw new IllegalArgumentException("Matrix row dimensions must agree.");
            }
            if (!this.isFullRank()) {
                throw new RuntimeException("Matrix is rank deficient.");
            }

            // Copy right hand side
            int nx = B.getColumnDimension();
            double[][] X = B.getArrayCopy();

            // Compute Y = transpose(Q)*B
            for (int k = 0; k < n; k++) {
                for (int j = 0; j < nx; j++) {
                    double s = 0.0;
                    for (int i = k; i < m; i++) {
                        s += QR[i][k] * X[i][j];
                    }
                    s = -s / QR[k][k];
                    for (int i = k; i < m; i++) {
                        X[i][j] += s * QR[i][k];
                    }
                }
            }
            // Solve R*X = Y;
            for (int k = n - 1; k >= 0; k--) {
                for (int j = 0; j < nx; j++) {
                    X[k][j] /= Rdiag[k];
                }
                for (int i = 0; i < k; i++) {
                    for (int j = 0; j < nx; j++) {
                        X[i][j] -= X[k][j] * QR[i][k];
                    }
                }
            }
            return (new BounceMatrix(X, n, nx).getMatrix(0, n - 1, 0, nx - 1));
        }

        public double hypot(double a, double b) {
            double r;
            if (Math.abs(a) > Math.abs(b)) {
                r = b / a;
                r = Math.abs(a) * Math.sqrt(1 + r * r);
            } else if (b != 0) {
                r = a / b;
                r = Math.abs(b) * Math.sqrt(1 + r * r);
            } else {
                r = 0.0;
            }
            return r;
        }

    }
}
