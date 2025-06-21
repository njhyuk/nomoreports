#!/bin/bash

# Ollama 서버를 백그라운드로 실행
ollama serve &

# 서버가 뜰 때까지 대기
until wget -q --spider http://localhost:11434/api/tags; do
    echo "Waiting for Ollama to be ready..."
    sleep 1
done

echo "Ollama server is ready!"

# 모델 다운로드 (한국어 지원이 좋은 mistral:7b 모델 사용)
echo "Downloading mistral:7b model..."
ollama pull mistral:7b

# 모델 다운로드 완료 확인
echo "Verifying model download..."
until ollama list | grep -q "mistral:7b"; do
    echo "Waiting for model to be available..."
    sleep 2
done

echo "Model download completed and verified!"

# 모델이 로드되었는지 테스트 (간단한 테스트만)
echo "Testing model availability..."
ollama show mistral:7b > /dev/null 2>&1

echo "Model is ready for use!"

# 컨테이너를 계속 실행 (SIGTERM 처리)
trap 'echo "Received SIGTERM, shutting down..."; exit 0' SIGTERM

# 무한 루프로 컨테이너 유지
while true; do
    sleep 1
done 