/**
 * AeroScan PM2.5 - Web Client Application Logic
 */

document.addEventListener('DOMContentLoaded', () => {
  // State
  let currentStream = null;
  let currentFacingMode = 'environment';
  let isCameraActive = false;
  let cameraAnimationId = null;
  let lastFrameTime = performance.now();
  let frameCount = 0;
  let currentFps = 0;
  let currentLocation = { name: 'กรุงเทพมหานคร (GPS)', province: 'กรุงเทพมหานคร' };
  let scanHistory = JSON.parse(localStorage.getItem('aeroscan_history') || '[]');

  // DOM Elements
  const tabs = document.querySelectorAll('.tab-btn');
  const tabPanes = document.querySelectorAll('.tab-pane');
  const dropzone = document.getElementById('dropzone');
  const fileInput = document.getElementById('fileInput');
  const imagePreview = document.getElementById('imagePreview');
  const laserScanner = document.getElementById('laserScanner');
  const hudStatusBadge = document.getElementById('hudStatusBadge');
  const presetButtons = document.querySelectorAll('.btn-preset');

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
    locationText.textContent = 'กำลังค้นหาพิกัด...';
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        pos => {
          const lat = pos.coords.latitude.toFixed(2);
          const lon = pos.coords.longitude.toFixed(2);
          currentLocation = {
            name: `พิกัด ${lat}, ${lon}`,
            province: 'พิกัดปัจจุบัน'
          };
          locationText.textContent = `${currentLocation.name}`;
        },
        err => {
          console.warn('Geolocation denied, using default:', err.message);
          currentLocation = { name: 'กรุงเทพมหานคร', province: 'กรุงเทพมหานคร' };
          locationText.textContent = 'กรุงเทพมหานคร';
        },
        { timeout: 8000 }
      );
    } else {
      locationText.textContent = 'กรุงเทพมหานคร';
    }
  }

  btnRefreshLocation.addEventListener('click', updateGeolocation);
  updateGeolocation();

  // ==========================================
  // PROCEDURAL SCENE GENERATION
  // ==========================================
  function generateProceduralScene(sceneType) {
    const width = 480;
    const height = 360;
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    const ctx = canvas.getContext('2d');

    if (sceneType === 'CLEAR_MOUNTAIN') {
      // Azure sky
      const skyGrad = ctx.createLinearGradient(0, 0, 0, height * 0.65);
      skyGrad.addColorStop(0, '#1e82e6');
      skyGrad.addColorStop(1, '#87cefa');
      ctx.fillStyle = skyGrad;
      ctx.fillRect(0, 0, width, height);

      // Sharp distant mountain
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

      // Forefront dark forest ridge
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
      // Hazy grayish-cyan sky
      const skyGrad = ctx.createLinearGradient(0, 0, 0, height * 0.7);
      skyGrad.addColorStop(0, '#8cafc8');
      skyGrad.addColorStop(1, '#d7e1eb');
      ctx.fillStyle = skyGrad;
      ctx.fillRect(0, 0, width, height);

      // Distant buildings
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

      // Atmospheric haze layer
      ctx.fillStyle = 'rgba(220, 225, 230, 0.4)';
      ctx.fillRect(0, height * 0.35, width, height);

    } else if (sceneType === 'HEAVY_SMOG_CITY') {
      // Thick yellowish-gray smog sky
      const skyGrad = ctx.createLinearGradient(0, 0, 0, height * 0.8);
      skyGrad.addColorStop(0, '#cdc8b9');
      skyGrad.addColorStop(1, '#e1dccd');
      ctx.fillStyle = skyGrad;
      ctx.fillRect(0, 0, width, height);

      // Obscured distant city skyline
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

      // Dense particulate aerosol veil
      ctx.fillStyle = 'rgba(230, 225, 210, 0.65)';
      ctx.fillRect(0, 0, width, height);

      // Faint bridge/road
      ctx.fillStyle = '#8c877d';
      ctx.fillRect(0, height * 0.78, width, height);
    }

    return canvas.toDataURL('image/jpeg', 0.92);
  }

  // Preset button clicks
  presetButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const scene = btn.dataset.scene;
      const dataUrl = generateProceduralScene(scene);
      loadImageAndAnalyze(dataUrl);
    });
  });

  // ==========================================
  // FILE DRAG & DROP AND UPLOAD
  // ==========================================
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

  fileInput.addEventListener('change', e => {
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
      }, 300);
    };
  }

  // ==========================================
  // DISPLAY RESULTS & TELEMETRY
  // ==========================================
  function displayAnalysisResult(result) {
    const lvl = result.level;

    // Badge & Confidence
    levelBadge.textContent = lvl.titleTh;
    levelBadge.style.background = `${lvl.color}22`;
    levelBadge.style.borderColor = lvl.color;
    levelBadge.style.color = lvl.color;
    confidenceTag.textContent = `AI Confidence: ${result.confidencePercent}%`;

    // Numbers
    pm25Value.textContent = result.pm25.toFixed(1);
    pm25Value.style.color = lvl.color;
    aqiValue.textContent = result.aqi;
    aqiValue.style.color = lvl.color;

    // Status Banner
    statusTitleTh.textContent = lvl.titleTh;
    statusTitleEn.textContent = lvl.titleEn;
    statusBanner.style.borderLeftColor = lvl.color;
    statusBanner.style.background = `${lvl.color}14`;

    // Gauge Needle & Arc
    // Full arc is 283. Angle range is -90deg to +90deg (total 180deg).
    // PM2.5 normalized up to 150 µg/m³
    const normalized = Math.min(Math.max(result.pm25 / 150.0, 0.0), 1.0);
    const strokeOffset = 283 - (normalized * 283);
    gaugeActiveArc.style.strokeDashoffset = strokeOffset;

    const needleDeg = -90 + (normalized * 180);
    gaugeNeedle.setAttribute('transform', `rotate(${needleDeg} 120 120)`);
    gaugeNeedle.setAttribute('stroke', lvl.color);

    // Telemetry Box
    telemetryHaze.innerHTML = `${result.hazeIndexPercent}<span class="t-unit">%</span>`;
    telemetryExtinction.innerHTML = `${result.extinctionCoeff}<span class="t-unit">km⁻¹</span>`;
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
          facingMode: currentFacingMode,
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

      // Start live processing loop
      processCameraFeed();
    } catch (err) {
      console.error('Camera access error:', err);
      alert('ไม่สามารถเข้าถึงกล้องได้: ' + err.message + '\nกรุณาตรวจสอบสิทธิ์การใช้งานกล้องในเบราว์เซอร์');
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

    // Run inference every 200ms to balance performance & response
    if (now - lastInferenceTime > 200 && cameraVideo.readyState === cameraVideo.HAVE_ENOUGH_DATA) {
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

    // Render Table
    let html = '';
    scanHistory.forEach(rec => {
      html += `
        <tr>
          <td>${rec.time}</td>
          <td>${rec.location}</td>
          <td style="font-family: var(--font-mono); font-weight: 700; color: ${rec.color}">${rec.pm25}</td>
          <td style="font-family: var(--font-mono);">${rec.aqi}</td>
          <td>
            <span class="history-badge" style="background: ${rec.color}22; color: ${rec.color}; border: 1px solid ${rec.color}55;">
              ${rec.levelTh}
            </span>
          </td>
        </tr>
      `;
    });
    historyTableBody.innerHTML = html;

    // Render SVG Trend Chart
    renderTrendChart();
  }

  function renderTrendChart() {
    const data = [...scanHistory].reverse();
    if (data.length < 2) {
      renderEmptyChart();
      return;
    }

    const width = 500;
    const height = 160;
    const padX = 35;
    const padY = 20;

    const maxPm = Math.max(100, ...data.map(d => d.pm25));
    const stepX = (width - padX * 2) / (data.length - 1);

    const points = data.map((d, i) => {
      const x = padX + i * stepX;
      const y = height - padY - (d.pm25 / maxPm) * (height - padY * 2);
      return { x, y, pm: d.pm25, color: d.color };
    });

    const pathData = points.reduce((acc, pt, i) => {
      return i === 0 ? `M ${pt.x} ${pt.y}` : `${acc} L ${pt.x} ${pt.y}`;
    }, '');

    // Threshold line at 37.5 (Thai standard)
    const thresholdY = height - padY - (37.5 / maxPm) * (height - padY * 2);

    let circles = '';
    points.forEach(pt => {
      circles += `<circle cx="${pt.x}" cy="${pt.y}" r="4" fill="${pt.color}" stroke="#070b14" stroke-width="2" />`;
    });

    trendSvg.innerHTML = `
      <defs>
        <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#00b0ff" stop-opacity="0.3"/>
          <stop offset="100%" stop-color="#00b0ff" stop-opacity="0.0"/>
        </linearGradient>
      </defs>
      <!-- Grid line for Thai PM2.5 standard (37.5) -->
      <line x1="${padX}" y1="${thresholdY}" x2="${width - padX}" y2="${thresholdY}" stroke="rgba(255, 82, 82, 0.4)" stroke-dasharray="4 3" stroke-width="1.5" />
      <text x="${width - padX - 4}" y="${thresholdY - 5}" fill="rgba(255, 82, 82, 0.7)" font-size="10" text-anchor="end" font-family="JetBrains Mono">37.5 เกณฑ์ไทย</text>
      
      <!-- Area fill -->
      <path d="${pathData} L ${points[points.length - 1].x} ${height - padY} L ${points[0].x} ${height - padY} Z" fill="url(#chartGrad)" />
      
      <!-- Trend Line -->
      <path d="${pathData}" fill="none" stroke="#00b0ff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
      
      <!-- Data dots -->
      ${circles}
    `;
  }

  function renderEmptyChart() {
    trendSvg.innerHTML = `
      <text x="250" y="80" fill="rgba(255,255,255,0.2)" font-size="12" text-anchor="middle" font-family="Plus Jakarta Sans">
        บันทึกข้อมูลสแกนอย่างน้อย 2 ครั้ง เพื่อแสดงเส้นแนวโน้ม
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
  // QR CODE MODAL FOR STUDENTS
  // ==========================================
  const btnOpenQrModal = document.getElementById('btnOpenQrModal');
  const btnCloseQrModal = document.getElementById('btnCloseQrModal');
  const qrModalBackdrop = document.getElementById('qrModalBackdrop');
  const btnCopyUrl = document.getElementById('btnCopyUrl');

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
      const urlText = 'https://dairy-faster-perjurer.ngrok-free.dev';
      navigator.clipboard.writeText(urlText).then(() => {
        btnCopyUrl.textContent = 'คัดลอกแล้ว!';
        setTimeout(() => {
          btnCopyUrl.textContent = 'คัดลอก';
        }, 2000);
      });
    });
  }

  // Automatically load the clear mountain preset on initial page launch
  const initialPreset = generateProceduralScene('CLEAR_MOUNTAIN');
  loadImageAndAnalyze(initialPreset);
});
