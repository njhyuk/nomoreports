FROM ollama/ollama:latest

# wget 설치 (Ubuntu/Debian 기반이므로 apt 사용)
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# 초기화 스크립트 복사
COPY ollama-init.sh /ollama-init.sh
RUN chmod +x /ollama-init.sh

EXPOSE 11434

ENTRYPOINT ["/bin/bash", "/ollama-init.sh"] 