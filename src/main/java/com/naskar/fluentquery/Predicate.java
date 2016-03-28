package com.naskar.fluentquery;

public interface Predicate<T, R> {

	Query<T> eq(R value);

	Query<T> like(R value);

}