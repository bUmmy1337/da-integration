package net.bummy1337.daintegrate;

public interface ITransformer<TFrom, TTo> {
    TTo transform(TFrom input);
}
