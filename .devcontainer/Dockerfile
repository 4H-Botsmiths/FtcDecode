# Start from the existing Java 17 devcontainer image (Java 17 is required for compilation)
FROM mcr.microsoft.com/devcontainers/java:17


# Install Java 21 (required for the Java extension)
# Copied from https://github.com/devcontainers/images/blob/main/src/java/.devcontainer/Dockerfile
USER root
ENV TARGET_JAVA_VERSION=21
ENV JAVA_HOME=/usr/lib/jvm/msopenjdk-current
ENV PATH="${JAVA_HOME}/bin:${PATH}"
# Android SDK location and tools on PATH
ENV ANDROID_SDK_ROOT=/usr/local/android-sdk
ENV ANDROID_HOME=${ANDROID_SDK_ROOT}
ENV PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${PATH}"
# Default to UTF-8 file.encoding
ENV LANG=en_US.UTF-8

# Install Microsoft OpenJDK
RUN arch="$(dpkg --print-architecture)" \
	&& case "$arch" in \
		"amd64") \
			jdkUrl="https://aka.ms/download-jdk/microsoft-jdk-${TARGET_JAVA_VERSION}-linux-x64.tar.gz"; \
			;; \
		"arm64") \
			jdkUrl="https://aka.ms/download-jdk/microsoft-jdk-${TARGET_JAVA_VERSION}-linux-aarch64.tar.gz"; \
			;; \
		*) echo >&2 "error: unsupported architecture: '$arch'"; exit 1 ;; \
	esac \
	\
	&& wget --progress=dot:giga -O msopenjdk.tar.gz "${jdkUrl}" \
	&& wget --progress=dot:giga -O sha256sum.txt "${jdkUrl}.sha256sum.txt" \
	\
	&& sha256sumText=$(cat sha256sum.txt) \
	&& sha256=$(expr substr "${sha256sumText}" 1 64) \
	&& echo "${sha256} msopenjdk.tar.gz" | sha256sum --strict --check - \
	&& rm sha256sum.txt* \
	\
	&& mkdir -p "$JAVA_HOME" \
	&& tar --extract \
		--file msopenjdk.tar.gz \
		--directory "$JAVA_HOME" \
		--strip-components 1 \
		--no-same-owner \
	&& rm msopenjdk.tar.gz* \
	\
	&& ln -s ${JAVA_HOME} /docker-java-home \
	&& ln -s ${JAVA_HOME} /usr/local/openjdk-${TARGET_JAVA_VERSION}

# Install Android SDK command-line tools and the core packages needed for FTC builds.
RUN apt-get update \
	&& apt-get install --no-install-recommends -y unzip \
	&& rm -rf /var/lib/apt/lists/* \
	&& mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools" \
	&& wget --progress=dot:giga -O /tmp/commandlinetools.zip https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip \
	&& unzip -q /tmp/commandlinetools.zip -d /tmp/android-cmdline-tools \
	&& mv /tmp/android-cmdline-tools/cmdline-tools "${ANDROID_SDK_ROOT}/cmdline-tools/latest" \
	&& rm -rf /tmp/commandlinetools.zip /tmp/android-cmdline-tools \
	&& yes | sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" --licenses \
	&& sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" "platform-tools" "platforms;android-30" "build-tools;34.0.0"
	