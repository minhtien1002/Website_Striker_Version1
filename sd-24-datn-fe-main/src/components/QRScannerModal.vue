<template>
    <Teleport to="body">
        <div v-if="isVisible"
            class="fixed inset-0 flex items-center justify-center qr-scanner-modal"
            style="z-index: 9999; background: rgba(0, 0, 0, 0.5) !important;">
            <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl transform transition-all duration-300"
                :class="{ 'scale-100 opacity-100': isVisible, 'scale-95 opacity-0': !isVisible }">
            <div class="flex justify-between items-center mb-4">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Quét QR Hóa Đơn</h3>
                <button @click="closeModal"
                    class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
                    <Icon icon="solar:close-circle-bold" class="text-2xl" />
                </button>
            </div>

            <!-- Camera Preview -->
            <div class="mb-4">
                <div v-if="!cameraStarted" class="bg-gray-100 dark:bg-gray-700 rounded-lg p-8 text-center">
                    <Icon icon="solar:camera-bold" class="text-4xl text-gray-400 mb-2" />
                    <p class="text-gray-600 dark:text-gray-300 mb-4">Nhấn "Bắt đầu quét" để mở camera</p>

                    <!-- Camera Selection -->
                    <div v-if="availableCameras.length > 1" class="mb-4">
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Chọn camera:
                        </label>
                        <select v-model="selectedCameraId"
                            class="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white">
                            <option v-for="camera in availableCameras" :key="camera.id" :value="camera.id">
                                {{ camera.label }}
                            </option>
                        </select>
                    </div>

                    <button @click="startCamera" :disabled="isLoading"
                        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50">
                        <span v-if="isLoading">Đang khởi động...</span>
                        <span v-else>Bắt đầu quét</span>
                    </button>
                </div>

                <div v-else class="relative">
                    <video ref="videoElement" class="w-full h-64 bg-black rounded-lg object-cover" autoplay muted
                        playsinline style="min-height: 256px; display: block;"></video>
                    <div class="absolute inset-0 border-2 border-blue-500 rounded-lg pointer-events-none">
                        <div class="absolute top-4 left-4 w-6 h-6 border-l-2 border-t-2 border-blue-500"></div>
                        <div class="absolute top-4 right-4 w-6 h-6 border-r-2 border-t-2 border-blue-500"></div>
                        <div class="absolute bottom-4 left-4 w-6 h-6 border-l-2 border-b-2 border-blue-500"></div>
                        <div class="absolute bottom-4 right-4 w-6 h-6 border-r-2 border-b-2 border-blue-500"></div>
                    </div>

                    <!-- Debug info overlay (remove in production) -->
                    <div class="absolute bottom-2 left-2 bg-black bg-opacity-50 text-white text-xs p-2 rounded">
                        <div>Video: {{ videoElement?.videoWidth }}x{{ videoElement?.videoHeight }}</div>
                        <div>Ready: {{ videoElement?.readyState }}</div>
                        <div>Paused: {{ videoElement?.paused }}</div>
                    </div>
                </div>
            </div>

            <!-- Manual Input Option -->
            <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Hoặc nhập mã hóa đơn thủ công:
                </label>
                <div class="flex gap-2">
                    <input v-model="manualCode" type="text" placeholder="Nhập mã hóa đơn..."
                        class="flex-1 p-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                        @keyup.enter="searchInvoice" />
                    <button @click="searchInvoice" :disabled="!manualCode.trim() || isSearching"
                        class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50">
                        <Icon v-if="isSearching" icon="solar:refresh-bold" class="animate-spin" />
                        <Icon v-else icon="solar:magnifer-bold" />
                    </button>
                </div>
            </div>

            <!-- Status Messages -->
            <div v-if="statusMessage" class="mb-4 p-3 rounded-lg" :class="statusMessageClass">
                <div class="flex items-center gap-2">
                    <Icon :icon="statusIcon" class="text-lg" />
                    <span class="text-sm">{{ statusMessage }}</span>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex gap-3">
                <button @click="closeModal"
                    class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                    Đóng
                </button>
                <button v-if="cameraStarted" @click="stopCamera"
                    class="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                    Dừng camera
                </button>
            </div>
            </div>
        </div>
    </Teleport>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { Icon } from '@iconify/vue';
import { useRouter } from 'vue-router';
import { useToast } from 'vue-toastification';
import { getHoaDonByMa } from '@/services/api/APIHoaDon/HoaDonAPI.js';

// Props
const props = defineProps({
    isVisible: {
        type: Boolean,
        default: false
    }
});

// Emits
const emit = defineEmits(['close', 'invoice-found']);

// Composables
const router = useRouter();
const toast = useToast();

// Reactive state
const videoElement = ref(null);
const cameraStarted = ref(false);
const isLoading = ref(false);
const isSearching = ref(false);
const manualCode = ref('');
const statusMessage = ref('');
const statusMessageClass = ref('');
const statusIcon = ref('');
const availableCameras = ref([]);
const selectedCameraId = ref('');

// Camera and QR detection
let stream = null;
let animationFrame = null;
let scanningTimeouts = []; // Track all timeouts for cleanup

// Methods
const closeModal = () => {
    // Ensure camera is completely stopped before closing
    stopCamera();

    // Additional cleanup - clear any remaining timeouts and ALL stream references
    setTimeout(() => {
        // Force cleanup any remaining references
        const allStreamRefs = [window.currentStream, window.qrScannerStream];
        allStreamRefs.forEach(streamRef => {
            if (streamRef) {
                streamRef.getTracks().forEach(track => {
                    console.log('🧹 Final cleanup - stopping track:', track.label);
                    track.stop();
                });
            }
        });

        // Clear all global references
        window.currentStream = null;
        window.qrScannerStream = null;

        // Force browser to release camera permissions
        navigator.mediaDevices.getUserMedia({ video: false, audio: false })
            .then(stream => {
                stream.getTracks().forEach(track => track.stop());
            })
            .catch(() => {
                // Ignore errors, this is just cleanup
            });

        console.log('🧹 Final cleanup completed');
    }, 100);

    emit('close');
};

const resetState = () => {
    manualCode.value = '';
    statusMessage.value = '';
    cameraStarted.value = false;
    isLoading.value = false;
    isSearching.value = false;
    availableCameras.value = [];
    selectedCameraId.value = '';
};

// Get available cameras
const getCameras = async () => {
    try {
        const devices = await navigator.mediaDevices.enumerateDevices();
        const cameras = devices.filter(device => device.kind === 'videoinput');
        availableCameras.value = cameras.map(camera => ({
            id: camera.deviceId,
            label: camera.label || `Camera ${cameras.indexOf(camera) + 1}`
        }));

        if (cameras.length > 0 && !selectedCameraId.value) {
            selectedCameraId.value = cameras[0].deviceId;
        }

        return cameras.length > 0;
    } catch (error) {
        console.error('Error getting cameras:', error);
        return false;
    }
};

const startCamera = async () => {
    isLoading.value = true;

    try {
        // Check if mediaDevices is supported
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            throw new Error('Camera API không được hỗ trợ trên trình duyệt này');
        }

        // Check if running on HTTPS or localhost
        const isSecureContext = window.isSecureContext || location.hostname === 'localhost' || location.hostname === '127.0.0.1';
        if (!isSecureContext) {
            showStatus('Camera chỉ hoạt động trên HTTPS hoặc localhost', 'error');
            return;
        }

        // First, set camera started to show video element
        cameraStarted.value = true;

        // Wait for DOM to update with video element
        await nextTick();
        await new Promise(resolve => setTimeout(resolve, 200));

        // Get available cameras first
        await getCameras();

        // Try different camera configurations
        const constraints = [];

        // If specific camera selected, use it
        if (selectedCameraId.value) {
            constraints.push({
                video: {
                    deviceId: { exact: selectedCameraId.value },
                    width: { ideal: 640 },
                    height: { ideal: 480 }
                }
            });
        }

        // Fallback constraints
        constraints.push(
            // Try back camera first (mobile)
            {
                video: {
                    facingMode: { exact: 'environment' },
                    width: { ideal: 640 },
                    height: { ideal: 480 }
                }
            },
            // Fallback to any camera
            {
                video: {
                    facingMode: 'environment',
                    width: { ideal: 640 },
                    height: { ideal: 480 }
                }
            },
            // Basic video constraint
            {
                video: {
                    width: { ideal: 640 },
                    height: { ideal: 480 }
                }
            },
            // Minimal constraint
            {
                video: true
            }
        );

        let stream = null;
        let lastError = null;

        // Try each constraint until one works
        for (const constraint of constraints) {
            try {
                console.log('Trying camera constraint:', constraint);
                stream = await navigator.mediaDevices.getUserMedia(constraint);
                console.log('✅ Camera constraint successful:', constraint);
                console.log('Stream active:', stream.active);
                console.log('Video tracks:', stream.getVideoTracks());
                break;
            } catch (error) {
                console.warn('❌ Camera constraint failed:', constraint, error);
                lastError = error;
                continue;
            }
        }

        if (!stream) {
            cameraStarted.value = false;
            throw lastError || new Error('Không thể truy cập camera với bất kỳ cấu hình nào');
        }

        console.log('Camera stream obtained:', stream);
        console.log('Stream tracks:', stream.getTracks());

        // Store stream in both global and local references for cleanup
        window.currentStream = stream;
        // Also store in local variable for double cleanup
        window.qrScannerStream = stream;

        console.log('Checking video element:', videoElement.value);
        console.log('Video element exists:', !!videoElement.value);

        if (!videoElement.value) {
            // Try to find video element by query selector as fallback
            const videoEl = document.querySelector('.qr-scanner-modal video');
            console.log('Fallback video element:', videoEl);

            if (videoEl) {
                // Manually assign to ref
                videoElement.value = videoEl;
            }
        }

        if (videoElement.value) {
            console.log('Video element found, setting srcObject');

            // Set stream to video element
            videoElement.value.srcObject = stream;

            // Set video attributes
            videoElement.value.setAttribute('playsinline', 'true');
            videoElement.value.setAttribute('autoplay', 'true');
            videoElement.value.setAttribute('muted', 'true');

            // Wait for video to load with better error handling
            await new Promise((resolve, reject) => {
                const timeout = setTimeout(() => {
                    console.error('Timeout waiting for video to load');
                    reject(new Error('Timeout waiting for video to load'));
                }, 15000);

                const onLoadedMetadata = () => {
                    console.log('Video metadata loaded');
                    clearTimeout(timeout);
                    videoElement.value.removeEventListener('loadedmetadata', onLoadedMetadata);
                    videoElement.value.removeEventListener('error', onError);
                    resolve();
                };

                const onError = (error) => {
                    console.error('Video error:', error);
                    clearTimeout(timeout);
                    videoElement.value.removeEventListener('loadedmetadata', onLoadedMetadata);
                    videoElement.value.removeEventListener('error', onError);
                    reject(error);
                };

                videoElement.value.addEventListener('loadedmetadata', onLoadedMetadata);
                videoElement.value.addEventListener('error', onError);

                // If video is already loaded
                if (videoElement.value.readyState >= 1) {
                    onLoadedMetadata();
                }
            });

            // Force play video
            try {
                console.log('Attempting to play video');
                const playPromise = videoElement.value.play();

                if (playPromise !== undefined) {
                    await playPromise;
                    console.log('Video play successful');
                }
            } catch (playError) {
                console.warn('Video play failed:', playError);
                // Try to continue anyway
            }

            // Verify video is actually playing
            setTimeout(() => {
                if (videoElement.value) {
                    console.log('Video state check:', {
                        readyState: videoElement.value.readyState,
                        videoWidth: videoElement.value.videoWidth,
                        videoHeight: videoElement.value.videoHeight,
                        paused: videoElement.value.paused,
                        currentTime: videoElement.value.currentTime
                    });
                }
            }, 1000);

            showStatus('Camera đã sẵn sàng. Hướng camera vào mã QR hóa đơn.', 'info');

            // Start QR detection after a shorter delay for faster response
            setTimeout(() => {
                detectQR();
            }, 800);
        } else {
            cameraStarted.value = false;
            throw new Error('Video element not found after DOM update');
        }
    } catch (error) {
        console.error('Error accessing camera:', error);
        cameraStarted.value = false;

        let errorMessage = 'Không thể truy cập camera. ';

        if (error.name === 'NotAllowedError') {
            errorMessage += 'Vui lòng cấp quyền truy cập camera trong trình duyệt.';
        } else if (error.name === 'NotFoundError') {
            errorMessage += 'Không tìm thấy camera trên thiết bị.';
        } else if (error.name === 'NotReadableError') {
            errorMessage += 'Camera đang được sử dụng bởi ứng dụng khác.';
        } else if (error.name === 'OverconstrainedError') {
            errorMessage += 'Cấu hình camera không được hỗ trợ.';
        } else if (error.name === 'SecurityError') {
            errorMessage += 'Lỗi bảo mật. Vui lòng sử dụng HTTPS.';
        } else {
            errorMessage += error.message || 'Lỗi không xác định.';
        }

        showStatus(errorMessage, 'error');
    } finally {
        isLoading.value = false;
    }
};

const stopCamera = () => {
    console.log('🛑 Stopping camera completely...');

    // Set flag to stop all scanning operations immediately
    cameraStarted.value = false;

    // Cancel animation frame if exists
    if (animationFrame) {
        cancelAnimationFrame(animationFrame);
        animationFrame = null;
    }

    // Clear all scanning timeouts
    scanningTimeouts.forEach(timeoutId => {
        clearTimeout(timeoutId);
    });
    scanningTimeouts = [];

    // Stop ALL possible stream references
    const streamsToStop = [
        window.currentStream,
        window.qrScannerStream,
        stream, // Local stream variable
    ];

    streamsToStop.forEach((streamRef, index) => {
        if (streamRef) {
            console.log(`📹 Stopping stream reference ${index + 1}...`);
            streamRef.getTracks().forEach(track => {
                console.log(`🔴 Stopping ${track.kind} track:`, track.label, 'State:', track.readyState);
                track.stop();

                // Force track to ended state
                if (track.readyState !== 'ended') {
                    console.warn(`⚠️ Track ${track.label} not properly ended, forcing...`);
                }
            });
        }
    });

    // Clear all stream references
    window.currentStream = null;
    window.qrScannerStream = null;
    stream = null;

    // Clear video element source with multiple attempts
    if (videoElement.value) {
        videoElement.value.pause();
        videoElement.value.srcObject = null;

        // Force video element to release camera
        videoElement.value.load();
        console.log('📺 Video element cleared, paused and reloaded');
    }

    // Additional browser-specific cleanup
    try {
        // Force garbage collection if available (Chrome DevTools)
        if (window.gc) {
            window.gc();
        }
    } catch (e) {
        // Ignore if gc not available
    }

    // Clear status
    statusMessage.value = '';

    console.log('🏁 Camera completely stopped and all references cleared');
};

const detectQR = async () => {
    if (!cameraStarted.value || !videoElement.value) return;

    try {
        // Import QR detection library dynamically
        const { BrowserQRCodeReader, DecodeHintType, BarcodeFormat } = await import('@zxing/library');

        // Configure QR reader with optimized settings
        const hints = new Map();
        hints.set(DecodeHintType.POSSIBLE_FORMATS, [BarcodeFormat.QR_CODE]);
        hints.set(DecodeHintType.TRY_HARDER, true);

        const codeReader = new BrowserQRCodeReader(hints);

        // Wait for video to be ready
        await new Promise((resolve) => {
            if (videoElement.value.readyState >= 2) {
                resolve();
            } else {
                videoElement.value.addEventListener('loadeddata', resolve, { once: true });
            }
        });

        let scanCount = 0;
        let lastScanTime = 0;

        // High-frequency scanning for better sensitivity
        const scanQR = async () => {
            if (!cameraStarted.value || !videoElement.value) return;

            const currentTime = Date.now();
            scanCount++;

            try {
                const result = await codeReader.decodeOnceFromVideoDevice(undefined, videoElement.value);
                if (result && result.getText()) {
                    const qrText = result.getText().trim();
                    console.log('🎯 QR Code detected:', qrText, `(scan #${scanCount})`);

                    // Stop scanning immediately after successful detection
                    if (animationFrame) {
                        cancelAnimationFrame(animationFrame);
                        animationFrame = null;
                    }

                    // Stop camera IMMEDIATELY and completely
                    stopCamera();

                    // Handle result after camera is stopped
                    await handleQRResult(qrText);
                    return;
                }
            } catch (error) {
                // Continue scanning if no QR found
                if (error.name !== 'NotFoundException') {
                    console.warn('QR scanning error:', error);
                }
            }

            // Log scanning performance every 50 scans
            if (scanCount % 50 === 0) {
                const elapsed = currentTime - lastScanTime;
                console.log(`📊 QR Scan performance: ${scanCount} scans, ${elapsed}ms since last log`);
                lastScanTime = currentTime;
            }

            // Schedule next scan with higher frequency (reduced delay)
            if (cameraStarted.value) {
                // Use setTimeout for more consistent timing than requestAnimationFrame
                const timeoutId = setTimeout(() => {
                    // Double-check camera is still active before next scan
                    if (cameraStarted.value && videoElement.value && videoElement.value.srcObject) {
                        scanQR();
                    }
                }, 50); // Scan every 50ms for better sensitivity

                // Track timeout for cleanup
                scanningTimeouts.push(timeoutId);
            }
        };

        // Start scanning
        lastScanTime = Date.now();
        scanQR();
    } catch (error) {
        console.error('QR detection initialization error:', error);
        showStatus('Lỗi khởi tạo quét QR. Vui lòng sử dụng nhập thủ công.', 'error');
    }
};

const handleQRResult = async (qrText) => {
    showStatus('🎯 Đã phát hiện QR code, đang tìm kiếm hóa đơn...', 'info');

    // Add vibration feedback if supported
    if (navigator.vibrate) {
        navigator.vibrate(200);
    }

    // Add audio feedback
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
        gainNode.gain.setValueAtTime(0.1, audioContext.currentTime);

        oscillator.start();
        oscillator.stop(audioContext.currentTime + 0.1);
    } catch (error) {
        // Audio feedback failed, continue silently
    }

    await searchInvoiceByCode(qrText.trim());
};

const searchInvoice = async () => {
    if (!manualCode.value.trim()) return;
    await searchInvoiceByCode(manualCode.value.trim());
};

const searchInvoiceByCode = async (invoiceCode) => {
    if (!invoiceCode || invoiceCode.trim() === '') {
        showStatus('Mã hóa đơn không hợp lệ', 'error');
        return;
    }

    isSearching.value = true;
    try {
        console.log('Searching for invoice with code:', invoiceCode);
        const invoiceData = await getHoaDonByMa(invoiceCode);

        if (invoiceData && invoiceData.ma) {
            showStatus(`✅ Tìm thấy hóa đơn: ${invoiceData.ma}`, 'success');

            // Emit success event
            emit('invoice-found', invoiceData);

            // Auto-close modal and navigate after shorter delay
            setTimeout(() => {
                router.push({
                    name: 'HoaDonChiTiet',
                    params: { id: invoiceData.id },
                    query: { highlight: invoiceData.ma }
                });
                closeModal();
            }, 1000); // Reduced from 1500ms to 1000ms
        } else {
            showStatus(`❌ Không tìm thấy hóa đơn với mã: ${invoiceCode}`, 'error');

            // Restart camera for another scan attempt
            setTimeout(() => {
                if (!cameraStarted.value) {
                    startCamera();
                }
            }, 2000);
        }

    } catch (error) {
        console.error('Error searching invoice:', error);
        const errorMessage = error.response?.data?.message || `❌ Không tìm thấy hóa đơn với mã: ${invoiceCode}`;
        showStatus(errorMessage, 'error');

        // Restart camera for another scan attempt
        setTimeout(() => {
            if (!cameraStarted.value) {
                startCamera();
            }
        }, 2000);
    } finally {
        isSearching.value = false;
    }
};

const showStatus = (message, type) => {
    statusMessage.value = message;

    switch (type) {
        case 'success':
            statusMessageClass.value = 'bg-green-50 dark:bg-green-950 text-green-700 dark:text-green-300';
            statusIcon.value = 'solar:check-circle-bold';
            break;
        case 'error':
            statusMessageClass.value = 'bg-red-50 dark:bg-red-950 text-red-700 dark:text-red-300';
            statusIcon.value = 'solar:danger-triangle-bold';
            break;
        case 'info':
        default:
            statusMessageClass.value = 'bg-blue-50 dark:bg-blue-950 text-blue-700 dark:text-blue-300';
            statusIcon.value = 'solar:info-circle-bold';
            break;
    }
};

// Initialize cameras when modal opens
const initializeCameras = async () => {
    if (props.isVisible && availableCameras.value.length === 0) {
        await getCameras();
    }
};

// Watch for modal visibility changes
watch(() => props.isVisible, (newValue) => {
    if (newValue) {
        initializeCameras();
    }
});

// Lifecycle
onMounted(() => {
    if (props.isVisible) {
        initializeCameras();
    }
});

onUnmounted(() => {
    stopCamera();
});
</script>

<style scoped>
/* Add any custom styles if needed */
</style>
