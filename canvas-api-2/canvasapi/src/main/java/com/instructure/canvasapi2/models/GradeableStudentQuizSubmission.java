package com.instructure.canvasapi2.models;


import android.os.Parcel;
import android.support.annotation.Nullable;

import java.util.Date;

public class GradeableStudentQuizSubmission extends CanvasModel<GradeableStudentQuizSubmission> {

    private User student;
    private QuizSubmission quizSubmission;

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public QuizSubmission getQuizSubmission() {
        return quizSubmission;
    }

    public void setQuizSubmission(QuizSubmission quizSubmission) {
        this.quizSubmission = quizSubmission;
    }

    @Override
    public long getId() {
        if(student != null) {
            return student.getId();
        } else {
            return 0;
        }
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        if (quizSubmission != null) {
            return quizSubmission.getComparisonDate();
        } else if(student != null) {
            return student.getComparisonDate();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getComparisonString() {
        if (quizSubmission != null) {
            return quizSubmission.getComparisonString();
        } else if(student != null){
            return student.getComparisonString();
        } else {
            return null;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.student, flags);
        dest.writeParcelable(this.quizSubmission, flags);
    }

    public GradeableStudentQuizSubmission() {
    }

    protected GradeableStudentQuizSubmission(Parcel in) {
        this.student = in.readParcelable(User.class.getClassLoader());
        this.quizSubmission = in.readParcelable(QuizSubmission.class.getClassLoader());
    }

    public static final Creator<GradeableStudentQuizSubmission> CREATOR = new Creator<GradeableStudentQuizSubmission>() {
        @Override
        public GradeableStudentQuizSubmission createFromParcel(Parcel source) {
            return new GradeableStudentQuizSubmission(source);
        }

        @Override
        public GradeableStudentQuizSubmission[] newArray(int size) {
            return new GradeableStudentQuizSubmission[size];
        }
    };
}
