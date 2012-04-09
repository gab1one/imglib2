/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2012 Stephan Preibisch, Stephan Saalfeld, Tobias
 * Pietzsch, Albert Cardona, Barry DeZonia, Curtis Rueden, Lee Kamentsky, Larry
 * Lindsey, Johannes Schindelin, Christian Dietz, Grant Harris, Jean-Yves
 * Tinevez, Steffen Jaensch, Mark Longair, Nick Perry, and Jan Funke.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */


package net.imglib2.ops.function.real;

import java.util.List;

import net.imglib2.ops.Function;
import net.imglib2.ops.PointSet;
import net.imglib2.type.numeric.RealType;

// Reference: Gonzalez and Woods, Digital Image Processing, 2008

/**
 * 
 * @author Barry DeZonia
 */
public class RealAdaptiveMedianFunction<T extends RealType<T>> 
	implements Function<PointSet,T>
{
	private final Function<long[],T> otherFunc;
	private final List<PointSet> pointSets;
	private final PrimitiveDoubleArray values;
	private final RealSampleCollector<T> collector;
	private final T currValue;
	private final StatCalculator calculator;
	
	public RealAdaptiveMedianFunction(Function<long[],T> otherFunc, List<PointSet> pointSets) {
		this.otherFunc = otherFunc;
		this.pointSets = pointSets;
		this.values = new PrimitiveDoubleArray();
		this.collector = new RealSampleCollector<T>();
		this.calculator = new StatCalculator();
		this.currValue = createOutput();
		if (pointSets.size() < 1)
			throw new IllegalArgumentException("must provide at least one point set");
	}
	
	@Override
	public void compute(PointSet points, T output) {
		double zMed = 0;
		for (int p = 0; p < pointSets.size(); p++) {
			PointSet pointSet = pointSets.get(p);
			pointSet.setAnchor(points.getAnchor());
			collector.collect(pointSet, otherFunc, values);
			zMed = calculator.median(values);
			// take advantage of fact that median() sorts values
			double zMin = values.get(0);
			double zMax = values.get(values.size()-1);
			double a1 = zMed - zMin;
			double a2 = zMed - zMax;
			if (a1 > 0 && a2 < 0) {
				otherFunc.compute(pointSet.getAnchor(), currValue);
				double zXY = currValue.getRealDouble();
				double b1 = zXY - zMin;
				double b2 = zXY - zMax;
				if ((b1 > 0) && (b2 < 0))
					output.setReal(zXY);
				else
					output.setReal(zMed);
				return;
			}
		}
		output.setReal(zMed);
	}

	@Override
	public RealAdaptiveMedianFunction<T> copy() {
		// TODO - do we need to copy() pointSets????? probably.
		return new RealAdaptiveMedianFunction<T>(otherFunc.copy(), pointSets);
	}

	@Override
	public T createOutput() {
		return otherFunc.createOutput();
	}
}

