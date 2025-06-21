package com.njhyuk.nomoreports.model

enum class CommitType(val label: String, val emoji: String) {
    FEATURE("기능 추가", "✨"),
    FIX("버그 수정", "🐛"),
    REFACTOR("리팩토링", "♻️"),
    TEST("테스트", "🧪"),
    DOCS("문서", "📚"),
    CHORE("기타", "🔧")
} 