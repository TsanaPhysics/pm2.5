/**
 * AeroScan PM2.5 - Ultra-Modern Web Client Application Logic
 * Full Responsive, Mobile Touch-Optimized, Dynamic Lighting & Real-time AI Engine
 */

document.addEventListener('DOMContentLoaded', () => {
  // Application State
  let currentStream = null;
  let currentFacingMode = 'environment';
  let isCameraActive = false;
  let cameraAnimationId = null;
  let lastFrameTime = performance.now();
  let frameCount = 0;
  let currentFps = 0;
  let currentLocation = { name: 'กรุงเทพมหานคร', province: 'กรุงเทพมหานคร' };
  let scanHistory = JSON.parse(localStorage.getItem('aeroscan_history') || '[]');

  let lastPm25 = 0;
  let lastAqi = 0;

  // DOM Elements
  const tabs = document.querySelectorAll('.tab-btn');
  const tabPanes = document.querySelectorAll('.tab-pane');
  const dropzone = document.getElementById('dropzone');
  const fileInput = document.getElementById('fileInput');
  const btnChooseFile = document.querySelector('.btn-choose-file');
  const imagePreview = document.getElementById('imagePreview');
  const laserScanner = document.getElementById('laserScanner');
  const hudStatusBadge = document.getElementById('hudStatusBadge');
  const presetButtons = document.querySelectorAll('.chip-btn, .btn-preset');

  // Camera Elements
  const btnToggleCam = document.getElementById('btnToggleCam');
  const btnSwitchCamera = document.getElementById('btnSwitchCamera');
  const cameraVideo = document.getElementById('cameraVideo');
  const cameraPlaceholder = document.getElementById('cameraPlaceholder');
  const camFps = document.getElementById('camFps');
  const camTransmission = document.getElementById('camTransmission');
  const camHaze = document.getElementById('camHaze');

  // Results & Telemetry Elements
  const levelBadge = document.getElementById('levelBadge');
  const confidenceTag = document.getElementById('confidenceTag');
  const gaugeActiveArc = document.getElementById('gaugeActiveArc');
  const gaugeNeedle = document.getElementById('gaugeNeedle');
  const pm25Value = document.getElementById('pm25Value');
  const aqiValue = document.getElementById('aqiValue');
  const statusBanner = document.getElementById('statusBanner');
  const statusTitleTh = document.getElementById('statusTitleTh');
  const statusTitleEn = document.getElementById('statusTitleEn');

  const telemetryHaze = document.getElementById('telemetryHaze');
  const telemetryExtinction = document.getElementById('telemetryExtinction');
  const telemetryTransmission = document.getElementById('telemetryTransmission');
  const telemetryContrast = document.getElementById('telemetryContrast');

  // Advisory Elements
  const advMask = document.getElementById('advMask');
  const advOutdoor = document.getElementById('advOutdoor');
  const advSensitive = document.getElementById('advSensitive');
  const advPurifier = document.getElementById('advPurifier');

  // Location Elements
  const locationText = document.getElementById('locationText');
  const btnRefreshLocation = document.getElementById('btnRefreshLocation');

  // History Elements
  const trendSvg = document.getElementById('trendSvg');
  const historyTableBody = document.getElementById('historyTableBody');
  const btnExportCsv = document.getElementById('btnExportCsv');
  const btnClearHistory = document.getElementById('btnClearHistory');

  // QR Modal Elements
  const btnOpenQrModal = document.getElementById('btnOpenQrModal');
  const btnCloseQrModal = document.getElementById('btnCloseQrModal');
  const qrModalBackdrop = document.getElementById('qrModalBackdrop');
  const btnCopyUrl = document.getElementById('btnCopyUrl');
  const btnShareWeb = document.getElementById('btnShareWeb');

  const PUBLIC_WEB_URL = 'https://dairy-faster-perjurer.ngrok-free.dev';

  // ==========================================
  // TAB NAVIGATION
  // ==========================================
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const target = tab.dataset.tab;
      tabs.forEach(t => t.classList.remove('active'));
      tabPanes.forEach(p => p.classList.remove('active'));

      tab.classList.add('active');
      if (target === 'photo') {
        document.getElementById('tabPhoto').classList.add('active');
        stopCamera();
      } else if (target === 'camera') {
        document.getElementById('tabCamera').classList.add('active');
      } else if (target === 'history') {
        document.getElementById('tabHistory').classList.add('active');
        stopCamera();
        renderHistory();
      }
    });
  });

  // ==========================================
  // GPS & LOCATION
  // ==========================================
  function updateGeolocation() {
    locationText.textContent = 'ค้นหาพิกัด...';
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        pos => {
          const lat = pos.coords.latitude.toFixed(2);
          const lon = pos.coords.longitude.toFixed(2);
          currentLocation = {
            name: `${lat}, ${lon}`,
            province: 'พิกัดปัจจุบัน'
          };
          locationText.textContent = `พิกัด ${currentLocation.name}`;
        },
        err => {
          console.warn('Geolocation denied, using default:', err.message);
          currentLocation = { name: 'กรุงเทพมหานคร', province: 'กรุงเทพมหานคร' };
          locationText.textContent = 'กรุงเทพมหานคร';
        },
        { timeout: 7000 }
      );
    } else {
      locationText.textContent = 'กรุงเทพมหานคร';
    }
  }

  if (btnRefreshLocation) {
    btnRefreshLocation.addEventListener('click', updateGeolocation);
    updateGeolocation();
  }

  // ==========================================
  // PROCEDURAL SCENE GENERATOR
  // ==========================================
  function generateProceduralScene(sceneType) {
    const width = 480;
    const height = 360;
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    const ctx = canvas.getContext('2d');

    if (sceneType === 'CLEAR_MOUNTAIN') {
      const skyGrad = ctx.createLinearGradient(0, 0, 0, height * 0.65);
      skyGrad.addColorStop(0, '#1e82e6');
      skyGrad.addColorStop(1, '#87cefa');
      ctx.fillStyle = skyGrad;
      ctx.fillRect(0, 0, width, height);

      ctx.fillStyle = '#2d5a41';
      ctx.beginPath();
      ctx.moveTo(0, height * 0.65);
      ctx.lineTo(width * 0.25, height * 0.42);
      ctx.lineTo(width * 0.55, height * 0.58);
      ctx.lineTo(width * 0.85, height * 0.38);
      ctx.lineTo(width, height * 0.60);
      ctx.lineTo(width, height);
      ctx.lineTo(0, height);
      ctx.closePath();
      ctx.fill();

      ctx.fillStyle = '#143c23';
      ctx.beginPath();
      ctx.moveTo(0, height * 0.75);
      ctx.lineTo(width * 0.35, height * 0.62);
      ctx.lineTo(width * 0.70, height * 0.70);
      ctx.lineTo(width, height * 0.65);
      ctx.lineTo(width, height);
      ctx.lineTo(0, height);
      ctx.closePath();
      ctx.fill();

    } else if (sceneType === 'MODERATE_SUBURB') {
      const skyGrad = ctx.createLinearGradient(0, 0, 0, height * 0.7);
      skyGrad.addColorStop(0, '#8cafc8');
      skyGrad.addColorStop(1, '#d7e1eb');
      ctx.fillStyle = skyGrad;
      ctx.fillRect(0, 0, width, height);

      ctx.fillStyle = '#788791';
      ctx.beginPath();
      ctx.moveTo(0, height * 0.62);
      ctx.lineTo(width * 0.15, height * 0.62);
      ctx.lineTo(width * 0.15, height * 0.48);
      ctx.lineTo(width * 0.25, height * 0.48);
      ctx.lineTo(width * 0.25, height * 0.65);
      ctx.lineTo(width * 0.40, height * 0.65);
      ctx.lineTo(width * 0.40, height * 0.44);
      ctx.lineTo(width * 0.52, height * 0.44);
      ctx.lineTo(width * 0.52, height * 0.63);
      ctx.lineTo(width * 0.75, height * 0.63);
      ctx.lineTo(width * 0.75, height * 0.52);
      ctx.lineTo(width * 0.88, height * 0.52);
      ctx.lineTo(width * 0.88, height * 0.65);
      ctx.lineTo(width, height * 0.65);
      ctx.lineTo(width, height);
      ctx.lineTo(0, height);
      ctx.closePath();
      ctx.fill();

      ctx.fillStyle = 'rgba(220, 225, 230, 0.4)';
      ctx.fillRect(0, height * 0.35, width, height);

    } else if (sceneType === 'HEAVY_SMOG_CITY') {
      const skyGrad = ctx.createLinearGradient(0, 0, 0, height * 0.8);
      skyGrad.addColorStop(0, '#cdc8b9');
      skyGrad.addColorStop(1, '#e1dccd');
      ctx.fillStyle = skyGrad;
      ctx.fillRect(0, 0, width, height);

      ctx.fillStyle = '#b4afa5';
      ctx.beginPath();
      ctx.moveTo(0, height * 0.60);
      ctx.lineTo(width * 0.2, height * 0.60);
      ctx.lineTo(width * 0.2, height * 0.45);
      ctx.lineTo(width * 0.35, height * 0.45);
      ctx.lineTo(width * 0.35, height * 0.60);
      ctx.lineTo(width * 0.6, height * 0.60);
      ctx.lineTo(width * 0.6, height * 0.40);
      ctx.lineTo(width * 0.75, height * 0.40);
      ctx.lineTo(width * 0.75, height * 0.60);
      ctx.lineTo(width, height * 0.60);
      ctx.lineTo(width, height);
      ctx.lineTo(0, height);
      ctx.closePath();
      ctx.fill();

      ctx.fillStyle = 'rgba(230, 225, 210, 0.65)';
      ctx.fillRect(0, 0, width, height);

      ctx.fillStyle = '#8c877d';
      ctx.fillRect(0, height * 0.78, width, height);
    }

    return canvas.toDataURL('image/jpeg', 0.92);
  }

  // Preset button listeners
  presetButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const scene = btn.dataset.scene;
      if (scene) {
        const dataUrl = generateProceduralScene(scene);
        loadImageAndAnalyze(dataUrl);
      }
    });
  });

  // ==========================================
  // FILE DRAG & DROP AND UPLOAD
  // ==========================================
  if (btnChooseFile && fileInput) {
    btnChooseFile.addEventListener('click', (e) => {
      e.stopPropagation();
      fileInput.click();
    });
  }

  ['dragenter', 'dragover'].forEach(eventName => {
    dropzone.addEventListener(eventName, e => {
      e.preventDefault();
      dropzone.classList.add('dragover');
    });
  });

  ['dragleave', 'drop'].forEach(eventName => {
    dropzone.addEventListener(eventName, e => {
      e.preventDefault();
      dropzone.classList.remove('dragover');
    });
  });

  dropzone.addEventListener('drop', e => {
    const files = e.dataTransfer.files;
    if (files.length > 0 && files[0].type.startsWith('image/')) {
      handleImageFile(files[0]);
    }
  });

  fileInput.addEventListener('change', () => {
    if (fileInput.files.length > 0) {
      handleImageFile(fileInput.files[0]);
    }
  });

  function handleImageFile(file) {
    const reader = new FileReader();
    reader.onload = evt => {
      loadImageAndAnalyze(evt.target.result);
    };
    reader.readAsDataURL(file);
  }

  function loadImageAndAnalyze(sourceUrl) {
    laserScanner.classList.add('scanning');
    hudStatusBadge.textContent = 'EXTRACTING DCP FEATURES...';
    imagePreview.src = sourceUrl;

    imagePreview.onload = () => {
      setTimeout(() => {
        const result = AirQualityVisionEngine.analyzeImageSource(imagePreview, currentLocation);
        displayAnalysisResult(result);
        saveScanResult(result);
        laserScanner.classList.remove('scanning');
        hudStatusBadge.textContent = `ANALYSIS DONE (${result.durationMs}ms)`;
      }, 250);
    };
  }

  // ==========================================
  // DISPLAY RESULTS & DYNAMIC THEME LIGHTING
  // ==========================================
  function hexToRgb(hex) {
    const clean = hex.replace('#', '');
    if (clean.length === 6) {
      const num = parseInt(clean, 16);
      return { r: (num >> 16) & 255, g: (num >> 8) & 255, b: num & 255 };
    }
    return null;
  }

  function animateNumber(element, start, end, duration = 400, decimals = 0) {
    const startTime = performance.now();
    function update(time) {
      const progress = Math.min((time - startTime) / duration, 1.0);
      const ease = 1 - Math.pow(1 - progress, 3);
      const current = start + (end - start) * ease;
      element.textContent = decimals > 0 ? current.toFixed(decimals) : Math.round(current);
      if (progress < 1.0) {
        requestAnimationFrame(update);
      }
    }
    requestAnimationFrame(update);
  }

  function displayAnalysisResult(result) {
    const lvl = result.level;

    // Dynamic Ambient Theme Lighting shift
    document.documentElement.style.setProperty('--theme-accent', lvl.color);
    const rgb = hexToRgb(lvl.color);
    if (rgb) {
      document.documentElement.style.setProperty('--theme-accent-rgb', `${rgb.r}, ${rgb.g}, ${rgb.b}`);
    }

    // Badge & Confidence
    levelBadge.textContent = lvl.titleTh;
    levelBadge.style.background = `${lvl.color}1e`;
    levelBadge.style.borderColor = lvl.color;
    levelBadge.style.color = lvl.color;
    confidenceTag.textContent = `AI Confidence: ${result.confidencePercent}%`;

    // Numbers animation
    animateNumber(pm25Value, lastPm25, result.pm25, 350, 1);
    animateNumber(aqiValue, lastAqi, result.aqi, 350, 0);
    lastPm25 = result.pm25;
    lastAqi = result.aqi;

    pm25Value.style.color = lvl.color;
    aqiValue.style.color = lvl.color;

    // Status Banner
    statusTitleTh.textContent = lvl.titleTh;
    statusTitleEn.textContent = lvl.titleEn;
    statusBanner.style.borderLeftColor = lvl.color;
    statusBanner.style.background = `${lvl.color}14`;

    // Radial Gauge
    const normalized = Math.min(Math.max(result.pm25 / 150.0, 0.0), 1.0);
    const strokeOffset = 289 - (normalized * 289);
    gaugeActiveArc.style.strokeDashoffset = strokeOffset;

    const needleDeg = -90 + (normalized * 180);
    gaugeNeedle.setAttribute('transform', `rotate(${needleDeg} 120 120)`);
    gaugeNeedle.setAttribute('stroke', lvl.color);

    // Telemetry Cards
    telemetryHaze.innerHTML = `${result.hazeIndexPercent}<span class="unit">%</span>`;
    telemetryExtinction.innerHTML = `${result.extinctionCoeff}<span class="unit">km⁻¹</span>`;
    telemetryTransmission.textContent = result.transmission;
    telemetryContrast.textContent = result.contrastEntropy;

    // Health Advisories
    advMask.textContent = lvl.maskRecommendation;
    advOutdoor.textContent = lvl.outdoorRecommendation;
    advSensitive.textContent = lvl.sensitiveGroupWarning;
    advPurifier.textContent = lvl.airPurifierAdvice;
  }

  // ==========================================
  // LIVE CAMERA HUD MODE
  // ==========================================
  btnToggleCam.addEventListener('click', () => {
    if (isCameraActive) {
      stopCamera();
    } else {
      startCamera();
    }
  });

  btnSwitchCamera.addEventListener('click', () => {
    currentFacingMode = currentFacingMode === 'environment' ? 'user' : 'environment';
    if (isCameraActive) {
      stopCamera();
      startCamera();
    }
  });

  async function startCamera() {
    try {
      const constraints = {
        video: {
          facingMode: { ideal: currentFacingMode },
          width: { ideal: 640 },
          height: { ideal: 480 }
        },
        audio: false
      };

      currentStream = await navigator.mediaDevices.getUserMedia(constraints);
      cameraVideo.srcObject = currentStream;
      cameraPlaceholder.style.display = 'none';
      btnToggleCam.textContent = 'ปิดกล้องสแกนสด';
      btnToggleCam.classList.add('active');
      isCameraActive = true;

      processCameraFeed();
    } catch (err) {
      console.error('Camera access error:', err);
      alert('ไม่สามารถเปิดกล้องได้: ' + err.message + '\nกรุณาตรวจสอบการอนุญาตใช้งานกล้องในเบราว์เซอร์');
    }
  }

  function stopCamera() {
    if (cameraAnimationId) {
      cancelAnimationFrame(cameraAnimationId);
      cameraAnimationId = null;
    }
    if (currentStream) {
      currentStream.getTracks().forEach(track => track.stop());
      currentStream = null;
    }
    cameraPlaceholder.style.display = 'flex';
    btnToggleCam.textContent = 'เปิดกล้องสแกนสด';
    btnToggleCam.classList.remove('active');
    isCameraActive = false;
  }

  let lastInferenceTime = 0;
  function processCameraFeed() {
    if (!isCameraActive) return;

    const now = performance.now();
    frameCount++;
    if (now - lastFrameTime >= 1000) {
      currentFps = frameCount;
      frameCount = 0;
      lastFrameTime = now;
      camFps.textContent = `FPS: ${currentFps}`;
    }

    if (now - lastInferenceTime > 220 && cameraVideo.readyState === cameraVideo.HAVE_ENOUGH_DATA) {
      lastInferenceTime = now;
      const result = AirQualityVisionEngine.analyzeImageSource(cameraVideo, currentLocation);
      displayAnalysisResult(result);
      camTransmission.textContent = `t: ${result.transmission}`;
      camHaze.textContent = `Haze: ${result.hazeIndexPercent}%`;
    }

    cameraAnimationId = requestAnimationFrame(processCameraFeed);
  }

  // ==========================================
  // HISTORY & ANALYTICS
  // ==========================================
  function saveScanResult(result) {
    const record = {
      id: Date.now(),
      time: new Date().toLocaleTimeString('th-TH', { hour: '2-digit', minute: '2-digit' }),
      date: new Date().toLocaleDateString('th-TH'),
      location: result.location.name,
      pm25: result.pm25,
      aqi: result.aqi,
      levelKey: result.level.key,
      levelTh: result.level.titleTh,
      color: result.level.color
    };

    scanHistory.unshift(record);
    if (scanHistory.length > 25) {
      scanHistory.pop();
    }
    localStorage.setItem('aeroscan_history', JSON.stringify(scanHistory));
  }

  function renderHistory() {
    if (scanHistory.length === 0) {
      historyTableBody.innerHTML = '<tr class="empty-row"><td colspan="5">ยังไม่มีประวัติการสแกน</td></tr>';
      renderEmptyChart();
      return;
    }

    let html = '';
    scanHistory.forEach(rec => {
      html += `
        <tr>
          <td>${rec.time}</td>
          <td>${rec.location}</td>
          <td style="font-family: var(--font-display); font-weight: 800; color: ${rec.color}">${rec.pm25}</td>
          <td style="font-family: var(--font-mono); font-weight: 600;">${rec.aqi}</td>
          <td>
            <span style="display:inline-block; padding: 0.15rem 0.5rem; border-radius: 4px; font-size: 0.72rem; font-weight: 700; background: ${rec.color}1e; color: ${rec.color}; border: 1px solid ${rec.color}55;">
              ${rec.levelTh}
            </span>
          </td>
        </tr>
      `;
    });
    historyTableBody.innerHTML = html;

    renderTrendChart();
  }

  function renderTrendChart() {
    const data = [...scanHistory].reverse();
    if (data.length < 2) {
      renderEmptyChart();
      return;
    }

    const width = 500;
    const height = 150;
    const padX = 35;
    const padY = 20;

    const maxPm = Math.max(80, ...data.map(d => d.pm25));
    const stepX = (width - padX * 2) / (data.length - 1);

    const points = data.map((d, i) => {
      const x = padX + i * stepX;
      const y = height - padY - (d.pm25 / maxPm) * (height - padY * 2);
      return { x, y, pm: d.pm25, color: d.color };
    });

    const pathData = points.reduce((acc, pt, i) => {
      return i === 0 ? `M ${pt.x} ${pt.y}` : `${acc} L ${pt.x} ${pt.y}`;
    }, '');

    const thresholdY = height - padY - (37.5 / maxPm) * (height - padY * 2);

    let circles = '';
    points.forEach(pt => {
      circles += `<circle cx="${pt.x}" cy="${pt.y}" r="4.5" fill="${pt.color}" stroke="#060913" stroke-width="2" />`;
    });

    trendSvg.innerHTML = `
      <defs>
        <linearGradient id="chartGlow" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#00b0ff" stop-opacity="0.32"/>
          <stop offset="100%" stop-color="#00b0ff" stop-opacity="0.0"/>
        </linearGradient>
      </defs>
      <line x1="${padX}" y1="${thresholdY}" x2="${width - padX}" y2="${thresholdY}" stroke="rgba(255, 82, 82, 0.45)" stroke-dasharray="4 3" stroke-width="1.5" />
      <text x="${width - padX - 4}" y="${thresholdY - 5}" fill="rgba(255, 82, 82, 0.85)" font-size="10" text-anchor="end" font-family="JetBrains Mono">37.5 เกณฑ์ไทย</text>
      
      <path d="${pathData} L ${points[points.length - 1].x} ${height - padY} L ${points[0].x} ${height - padY} Z" fill="url(#chartGlow)" />
      <path d="${pathData}" fill="none" stroke="#00b0ff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
      ${circles}
    `;
  }

  function renderEmptyChart() {
    trendSvg.innerHTML = `
      <text x="250" y="75" fill="rgba(255,255,255,0.25)" font-size="12" text-anchor="middle" font-family="Plus Jakarta Sans">
        สแกนอย่างน้อย 2 ครั้ง เพื่อแสดงกราฟแนวโน้มมลพิษ
      </text>
    `;
  }

  btnExportCsv.addEventListener('click', () => {
    if (scanHistory.length === 0) {
      alert('ไม่มีข้อมูลสำหรับส่งออก');
      return;
    }

    let csv = 'Timestamp,Location,PM2.5,AQI,Level\n';
    scanHistory.forEach(r => {
      csv += `"${r.date} ${r.time}","${r.location}",${r.pm25},${r.aqi},"${r.levelTh}"\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `AeroScan-History-${Date.now()}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  });

  btnClearHistory.addEventListener('click', () => {
    if (confirm('ต้องการล้างประวัติการสแกนทั้งหมดใช่หรือไม่?')) {
      scanHistory = [];
      localStorage.removeItem('aeroscan_history');
      renderHistory();
    }
  });

  // ==========================================
  // QR CODE MODAL & SHARE API
  // ==========================================
  if (btnOpenQrModal && qrModalBackdrop) {
    btnOpenQrModal.addEventListener('click', () => {
      qrModalBackdrop.classList.add('active');
    });

    btnCloseQrModal.addEventListener('click', () => {
      qrModalBackdrop.classList.remove('active');
    });

    qrModalBackdrop.addEventListener('click', (e) => {
      if (e.target === qrModalBackdrop) {
        qrModalBackdrop.classList.remove('active');
      }
    });

    btnCopyUrl.addEventListener('click', () => {
      navigator.clipboard.writeText(PUBLIC_WEB_URL).then(() => {
        btnCopyUrl.textContent = 'คัดลอกแล้ว!';
        setTimeout(() => {
          btnCopyUrl.textContent = 'คัดลอกลิงก์';
        }, 2000);
      });
    });
  }

  if (btnShareWeb) {
    btnShareWeb.addEventListener('click', async () => {
      if (navigator.share) {
        try {
          await navigator.share({
            title: 'AeroScan PM2.5',
            text: 'ตรวจวัดฝุ่น PM2.5 จากภาพถ่ายและกล้องสดด้วย AI',
            url: PUBLIC_WEB_URL
          });
        } catch (err) {
          if (err.name !== 'AbortError') {
            console.warn('Share error:', err);
          }
        }
      } else {
        qrModalBackdrop.classList.add('active');
      }
    });
  }

  // Initial preset
  const initialPreset = generateProceduralScene('CLEAR_MOUNTAIN');
  loadImageAndAnalyze(initialPreset);
});
