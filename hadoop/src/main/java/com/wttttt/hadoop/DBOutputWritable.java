package com.wttttt.hadoop;

import org.apache.hadoop.mapreduce.lib.db.DBWritable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DBOutputWritable implements DBWritable {
    // origin, predict, probability
    private String origin;
    private String predict;
    private int count;

    public DBOutputWritable(String origin, String predict, int count) {
        this.origin = origin;
        this.predict = predict;
        this.count = count;
    }


    public void write(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, origin);
        preparedStatement.setString(2, predict);
        preparedStatement.setInt(3, count);

    }

    public void readFields(ResultSet resultSet) throws SQLException {
        this.origin = resultSet.getString(1);
        this.predict = resultSet.getString(2);
        this.count = resultSet.getInt(3);
    }
}
