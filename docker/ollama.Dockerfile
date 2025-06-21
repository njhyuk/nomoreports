FROM ollama/ollama:latest

ARG OLLAMA_MODEL=mistral:7b

# Download the model during the build
RUN /bin/sh -c "ollama serve & sleep 10 && ollama pull ${OLLAMA_MODEL} && pkill ollama"

# The default entrypoint from ollama/ollama is `ollama serve`
# 컨테이너가 시작되면 자동으로 ollama serve가 실행됩니다.
EXPOSE 11434