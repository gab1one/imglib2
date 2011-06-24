/**
 * Copyright (c) 2009--2010, Stephan Preibisch & Stephan Saalfeld
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.  Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution.  Neither the name of the Fiji project nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.imglib2.img.array;

import net.imglib2.AbstractRandomAccessInt;
import net.imglib2.type.NativeType;

/**
 * 
 * @param <T>
 *
 * @author Stephan Preibisch, Stephan Saalfeld, Tobias Pietzsch
 */
public class ArrayRandomAccess< T extends NativeType< T > > extends AbstractRandomAccessInt< T >
{
	protected final T type;
	final ArrayImg< T, ? > container;
	
	protected ArrayRandomAccess( final ArrayRandomAccess< T > randomAccess ) 
	{
		super( randomAccess.numDimensions() );
		
		this.container = randomAccess.container;
		this.type = container.createLinkedType();
		
		int index = 0;
		for ( int d = 0; d < n; d++ )
		{
			position[ d ] = randomAccess.position[ d ];
			index += position[ d ] * container.steps[ d ];
		}
		
		type.updateContainer( this );
		type.updateIndex( index );
	}
	
	public ArrayRandomAccess( final ArrayImg< T, ? > container ) 
	{
		super( container.numDimensions() );
		
		this.container = container;
		this.type = container.createLinkedType();
		
		for ( int d = 0; d < n; d++ )
			position[ d ] = 0;

		type.updateContainer( this );
		type.updateIndex( 0 );
	}	
	
	@Override
	public T get()
	{
		return type;
	}
	
	@Override
	public void fwd( final int dim )
	{
		type.incIndex( container.steps[ dim ] );
		++position[ dim ];
	}

	@Override
	public void bck( final int dim )
	{
		type.decIndex( container.steps[ dim ] );
		--position[ dim ];
	}

	@Override
	public void move( final int distance, final int dim )
	{
		type.incIndex( container.steps[ dim ] * distance );
		position[ dim ] += distance;
	}
	
	@Override
	public void move( final long distance, final int dim )
	{
		type.incIndex( container.steps[ dim ] * ( int )distance );
		position[ dim ] += distance;
	}

	@Override
	public void setPosition( final int[] pos )
	{
		int index = 0;
		for ( int d = 0; d < n; ++d )
		{
			position[ d ] = pos[ d ];
			index += pos[ d ] * container.steps[ d ];
		}
		type.updateIndex( index );
	}
	
	@Override
	public void setPosition( long[] pos )
	{
		int index = 0;
		for ( int d = 0; d < n; ++d )
		{
			final int p = ( int )pos[ d ];
			position[ d ] = p;
			index += p * container.steps[ d ];
		}
		type.updateIndex( index );
	}

	@Override
	public void setPosition( final int pos, final int dim )
	{
		type.incIndex( ( pos - position[ dim ] ) * container.steps[ dim ] );
		position[ dim ] = pos;
	}
	
	@Override
	public ArrayRandomAccess< T > copy()
	{
		return new ArrayRandomAccess< T >( this );
	}

	@Override
	public ArrayRandomAccess< T > copyRandomAccess()
	{
		return copy();
	}
	
	/* Special methods for access in one-dimensional arrays only */
	
	/**
	 * Moves one step forward in dimension 0
	 */
	public void fwdDim0()
	{
		type.incIndex();
		++position[ 0 ];
	}

	/**
	 * Moves one step backward in dimension 0
	 */
	public void bckDim0()
	{
		type.decIndex();
		--position[ 0 ];
	}

	/**
	 * Moves n steps in dimension 0
	 * 
	 * @param distance - how many steps (positive or negative)
	 */
	public void moveDim0( final int distance )
	{
		type.incIndex( distance );
		position[ 0 ] += distance;
	}
	
	/**
	 * Moves n steps in dimension 0
	 * 
	 * @param distance - how many steps (positive or negative)
	 */
	public void move( final long distance )
	{
		type.incIndex( (int)distance );
		position[ 0 ] += distance;
	}

	/**
	 * Sets the {@link ArrayRandomAccess} to a certain position in dimension 0
	 * 
	 * Careful: it assumes that it is only a one-dimensional image, all other dimensions would be set to zero (this saves one subtraction)
	 * 
	 * @param pos - the new position
	 */
	public void setPositionDim0( final int pos )
	{
		type.updateIndex( pos );
		position[ 0 ] = pos;
	}

	/**
	 * Sets the {@link ArrayRandomAccess} to a certain position in dimension 0
	 * 
	 * Careful: it assumes that it is only a one-dimensional image, all other dimensions would be set to zero (this saves one subtraction)
	 * 
	 * @param pos - the new position
	 */
	public void setPositionDim0( final long pos )
	{
		type.updateIndex( (int)pos );
		position[ 0 ] = (int)pos;
	}
	
}