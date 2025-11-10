package com.example.glidedemo.views.pattern;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Represents a cell in the matrix of the pattern view
 */
public class Dot implements Parcelable {

    public final int mDotCount;
    public final int mRow;
    public final int mColumn;

    public Dot(int dotCount, int row, int column) {
        this.mDotCount = dotCount;
        this.mRow = row;
        this.mColumn = column;
    }

    /**
     * Gets the identifier of the dot. It is counted from left to right, top to bottom of the
     * matrix, starting by zero
     */
    public int getId() {
        return mRow * mDotCount + mColumn;
    }

    public int getRow() {
        return mRow;
    }

    public int getColumn() {
        return mColumn;
    }

    @NonNull
    @Override
    public String toString() {
        return "(Row = " + mRow + ", Col = " + mColumn + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Dot)
            return mColumn == ((Dot) object).mColumn
                    && mRow == ((Dot) object).mRow;
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        int result = mRow;
        result = 31 * result + mColumn;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mDotCount);
        dest.writeInt(mColumn);
        dest.writeInt(mRow);
    }

    public static final Creator<Dot> CREATOR = new Creator<Dot>() {

        public Dot createFromParcel(Parcel in) {
            return new Dot(in);
        }

        public Dot[] newArray(int size) {
            return new Dot[size];
        }
    };

    private Dot(Parcel in) {
        mDotCount = in.readInt();
        mColumn = in.readInt();
        mRow = in.readInt();
    }

}