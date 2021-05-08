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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
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

/**
 * A BounceParameters object is a set of parameters for
 * a BounceModel model, which is included by reference.
 * <p>
 * Wed Nov  2 15:30:20 PDT 2011 Kevin Karplus
 *
 * @author Kevin Karplus
 */
@Getter
public class BounceParameters {

    public BounceParameters(BounceModel model, BounceMatrix params, double squareError) {
        this.model = model;
        this.params = params;
        this.squareError = squareError;
        initialStepAt = 0;
        initialStepSize = null;
    }

    public BounceParameters(BounceModel model, BounceMatrix params,
                            double squareError, double stepAt, double[] stepSize) {
        this.model = model;
        this.params = params;
        this.squareError = squareError;
        initialStepAt = stepAt;
        initialStepSize = stepSize;
    }


    // The model that was fitted to create these parameters.
    private final BounceModel model;

    // The params matrix has as many rows as model has parameters.
    // It has as many columns as the dimensionality of the data being fitted.
    private final BounceMatrix params;

    // The sum of the squares of the residual error of the fitting.
    private final double squareError;

    // If a step was removed before fitting the model, when was the step?
    private final double initialStepAt;

    // If a step was removed before fitting the model, how big was it?
    private final double[] initialStepSize;


    /**
     * when is there a step?
     *
     * @return time of step in velocity
     */
    public double getStepAt() {
        double model_stepat = model.getStepAt(params);
        if (null == initialStepSize) return model_stepat;
        if (!model.usesStep() || model_stepat == initialStepAt) return initialStepAt;
        throw new RuntimeException("LinearModelParams with steps at different times"); //$NON-NLS-1$

    }

    /**
     * how big is the step?
     * <p>
     * The dimensionality of the returned array of doubles is the same as
     * the number of columns in the model_param Matrix
     * (that would be 2 for model_param returned by fit_xy()).
     *
     * @return step size (0 if no step)
     */
    public double[] getStepSize() {
        int dimension = params.getColumnDimension();
        double[] result = new double[dimension];

        if (initialStepSize != null) {
            System.arraycopy(initialStepSize, 0, result, 0, result.length);
            if (model.usesStep() && model.getStepAt() != initialStepAt) {
                throw new RuntimeException("LinearModelParams getStepSize with steps at different times"); //$NON-NLS-1$
            }
        }

        if (!model.usesStep()) {
            return result;
        }

        int step_index = params.getRowDimension() - 1;
        double[][] param_array = params.getArray();
        for (int dim = 0; dim < dimension; dim++) {
            result[dim] += param_array[step_index][dim];
        }
        return result;
    }


    /**
     * first derivatives of model at time t
     * <p>
     * time is "model" time, with t=0 as the first data point,
     * t=1 as the second data point,
     * ...
     * <p>
     * The dimensionality of the returned array of doubles is the same as
     * the number of columns in the params.
     *
     * @param t time
     * @return derivatives
     */
    public double[] first_deriv(double t) {
        double[] result = model.first_deriv(params, t);
        if (initialStepSize != null && initialStepAt < t) {
            for (int i = 0; i < result.length; i++) {
                result[i] += initialStepSize[i];
            }
        }
        return result;
    }

    /**
     * second derivatives of model at time t
     * <p>
     * time is "model" time, with t=0 as the second data point,
     * t=1 as the second data point,
     * ...
     * <p>
     * The dimensionality of the returned array of doubles is the same as
     * the number of columns in the params.
     *
     * @param t time
     * @return derivatives
     */
    public double[] second_deriv(double t) {
        double[] result = model.second_deriv(params, t);
        if (initialStepSize != null && Math.round(initialStepAt - t) == 0) {
            for (int i = 0; i < result.length; i++) {
                result[i] += initialStepSize[i];
            }
        }
        return result;
    }
}