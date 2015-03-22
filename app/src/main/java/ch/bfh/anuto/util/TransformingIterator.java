package ch.bfh.anuto.util;

import java.util.Iterator;

public class TransformingIterator<F, T> implements Iterator<T> {

    Function<F, T> mTransformation;
    Iterator<F> mOriginal;

    public TransformingIterator(Iterator<F> original, Function<F, T> transformation) {
        mOriginal = original;
        mTransformation = transformation;
    }

    @Override
    public boolean hasNext() {
        return mOriginal.hasNext();
    }

    @Override
    public T next() {
        return mTransformation.apply(mOriginal.next());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}