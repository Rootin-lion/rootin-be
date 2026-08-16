package com.example.rootin.bookmark.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class ProblemBookmarkNotFoundException extends CustomException {

    public ProblemBookmarkNotFoundException() {
        super(ErrorCode.PROBLEM_BOOKMARK_NOT_FOUND);
    }
}