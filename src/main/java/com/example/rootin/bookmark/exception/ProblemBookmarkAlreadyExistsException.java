package com.example.rootin.bookmark.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class ProblemBookmarkAlreadyExistsException extends CustomException {

    public ProblemBookmarkAlreadyExistsException() {
        super(ErrorCode.PROBLEM_BOOKMARK_ALREADY_EXISTS);
    }
}

