package tests;

import ij.ImageJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import mpicbg.imglib.Cursor;
import mpicbg.imglib.IterableInterval;
import mpicbg.imglib.RandomAccessible;
import mpicbg.imglib.display.ARGBScreenImage;
import mpicbg.imglib.display.RealARGBConverter;
import mpicbg.imglib.display.XYProjector;
import mpicbg.imglib.img.Img;
import mpicbg.imglib.img.ImgFactory;
import mpicbg.imglib.img.array.ArrayImgFactory;
import mpicbg.imglib.interpolation.linear.LinearInterpolator;
import mpicbg.imglib.interpolation.linear.LinearInterpolatorFactory;
import mpicbg.imglib.io.LOCI;
import mpicbg.imglib.type.numeric.ARGBType;
import mpicbg.imglib.type.numeric.NumericType;
import mpicbg.imglib.type.numeric.real.FloatType;


public class OpenAndDisplayInterpolated
{
	public static <T extends NumericType< T > > void copyInterpolatedGeneric( RandomAccessible< T > from, IterableInterval< T > to, double[] offset, double scale )
	{
		final int n = to.numDimensions();
		final double[] fromPosition = new double[ n ];
		Cursor< T > cursor = to.localizingCursor();
		LinearInterpolator< T > interpolator =  new LinearInterpolatorFactory< T >().create( from );
		while ( cursor.hasNext() )
		{
			final T t = cursor.next();
			for ( int d = 0; d < n; ++d )
			{
				fromPosition[ d ] = scale * cursor.getDoublePosition( d ) + offset[ d ];
			}
			interpolator.setPosition( fromPosition );
			t.set( interpolator.get() );
		}
	}

	final static public void main( final String[] args )
	{
		new ImageJ();
		
		ImgFactory< FloatType > imgFactory = new ArrayImgFactory< FloatType >();
		
		Img< FloatType > img = LOCI.openLOCIFloatType( "/home/tobias/workspace/imglibworkshop/DrosophilaWing.tif", imgFactory );

		Img< FloatType > interpolatedImg = imgFactory.create( new long[] {200, 200}, new FloatType () );
		
		final ARGBScreenImage screenImage = new ARGBScreenImage( ( int )interpolatedImg.dimension( 0 ), ( int )interpolatedImg.dimension( 1 ) );
		final XYProjector< FloatType, ARGBType > projector = new XYProjector< FloatType, ARGBType >( interpolatedImg, screenImage, new RealARGBConverter< FloatType >( 0, 255 ) );

		final ColorProcessor cp = new ColorProcessor( screenImage.image() );
		final ImagePlus imp = new ImagePlus( "argbScreenProjection", cp );
		imp.show();
		
		double[] offset = new double[] {50, 10};
		double scale = 1.0;
		for ( int i=0; i<2000; ++i ) {
			copyInterpolatedGeneric( img, interpolatedImg, offset, scale );
			projector.map();
			final ColorProcessor cpa = new ColorProcessor( screenImage.image() );
			imp.setProcessor( cpa );
			offset[0] += 0.2;
			offset[0] += 0.04;
			scale *= 0.999;
		}
	}
}
