package com.lookeypl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public class HomeCollectionCodec implements Codec<HomeCollection> {

    @Override
    public <T> DataResult<T> encode(HomeCollection input, DynamicOps<T> ops, T prefix) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encode'");
    }

    @Override
    public <T> DataResult<Pair<HomeCollection, T>> decode(DynamicOps<T> ops, T input) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

}
