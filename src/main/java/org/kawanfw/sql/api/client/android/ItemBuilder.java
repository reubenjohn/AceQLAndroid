package org.kawanfw.sql.api.client.android;

import java.sql.ResultSet;

public interface ItemBuilder<T> {
    T buildItem(ResultSet rs);
}
