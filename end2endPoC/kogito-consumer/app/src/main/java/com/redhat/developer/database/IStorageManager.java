package com.redhat.developer.database;

import java.util.List;

public interface IStorageManager {

    <T> boolean create(String key, T request, String index);

    <T> List<T> search(TrustyStorageQuery query, String index, Class<T> type);

    boolean deleteIndex(String index);
}
