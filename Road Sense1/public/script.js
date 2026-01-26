// ============================================================
// ROAD-SENSE WEB DASHBOARD - Main JavaScript
// ============================================================
// This file handles:
// 1. Firebase Firestore connection & real-time data
// 2. Leaflet map with marker clustering
// 3. Filtering by severity and time
// 4. Statistics calculation
// ============================================================

import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js';
import { 
  getFirestore, 
  collection, 
  query, 
  orderBy, 
  limit, 
  onSnapshot,
  where,
  Timestamp
} from 'https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js';

// ==================== FIREBASE CONFIGURATION ====================
// Road-Sense Firebase Project - roadsense-fb6cb

const firebaseConfig = {
  apiKey: "AIzaSyD1qlq6dlT5Ielyj9ANblqACKd6u__cppk",
  authDomain: "roadsense-fb6cb.firebaseapp.com",
  projectId: "roadsense-fb6cb",
  storageBucket: "roadsense-fb6cb.firebasestorage.app",
  messagingSenderId: "974723114346",
  appId: "1:974723114346:web:1edd1c81f3233585903113",
  measurementId: "G-D6PTW49WSP"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// ==================== CONFIGURATION ====================
const CONFIG = {
  // Map settings - Center on India
  MAP_CENTER: [20.5937, 78.9629],  // India center coordinates
  MAP_ZOOM: 5,                      // Zoom level to show all of India
  MIN_ZOOM: 4,                      // Allow zooming out to see more
  MAX_ZOOM: 19,
  
  // Data limits
  MAX_POTHOLES: 2000,  // Maximum potholes to load
  
  // Collection name in Firestore (must match Android app)
  COLLECTION_NAME: 'potholes',
  
  // Severity colors (using string keys: "low", "medium", "high")
  COLORS: {
    'low': '#28a745',     // Green
    'medium': '#fd7e14',  // Orange
    'high': '#dc3545'     // Red
  },
  
  // Severity labels
  LABELS: {
    'low': 'Low',
    'medium': 'Medium',
    'high': 'High'
  }
};

// ==================== DOM ELEMENTS ====================
const elements = {
  map: null,
  loading: document.getElementById('loading'),
  errorMessage: document.getElementById('errorMessage'),
  errorText: document.getElementById('errorText'),
  totalCount: document.getElementById('totalCount'),
  highCount: document.getElementById('highCount'),
  mediumCount: document.getElementById('mediumCount'),
  lowCount: document.getElementById('lowCount'),
  lastUpdate: document.getElementById('lastUpdate'),
  severityFilter: document.getElementById('severityFilter'),
  timeFilter: document.getElementById('timeFilter'),
  refreshBtn: document.getElementById('refreshBtn')
};

// ==================== STATE ====================
let map = null;
let markersCluster = null;
let currentUnsubscribe = null;  // To unsubscribe from Firestore listener
let allPotholes = [];  // Store all data for filtering

// ==================== INITIALIZE MAP ====================
function initMap() {
  // Create Leaflet map centered on INDIA
  map = L.map('map', {
    center: [22.5, 82.5],  // Center of India
    zoom: 5,               // Shows all of India
    zoomControl: true,
    minZoom: 4,
    maxZoom: 18
  });
  
  // USE GOOGLE MAPS TILES (Best quality, most reliable)
  L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
    maxZoom: 20,
    attribution: '&copy; Google Maps'
  }).addTo(map);
  
  // Initialize marker cluster group
  markersCluster = L.markerClusterGroup({
    maxClusterRadius: 60,
    disableClusteringAtZoom: 16,
    spiderfyOnMaxZoom: true,
    showCoverageOnHover: false,
    chunkedLoading: true,
    iconCreateFunction: createClusterIcon
  });
  
  map.addLayer(markersCluster);
  
  // Add scale control
  L.control.scale({ position: 'bottomleft' }).addTo(map);
  
  console.log('✅ Map initialized');
}

// Custom cluster icon based on severity
function createClusterIcon(cluster) {
  const markers = cluster.getAllChildMarkers();
  let highCount = 0, mediumCount = 0, lowCount = 0;
  
  markers.forEach(m => {
    const severity = m.options.severity || 'low';
    if (severity === 'high') highCount++;
    else if (severity === 'medium') mediumCount++;
    else lowCount++;
  });
  
  let className = 'marker-cluster-small';
  if (highCount > markers.length * 0.3) className = 'marker-cluster-large';
  else if (mediumCount > markers.length * 0.3) className = 'marker-cluster-medium';
  
  return L.divIcon({
    html: `<div><span>${markers.length}</span></div>`,
    className: `marker-cluster ${className}`,
    iconSize: L.point(40, 40)
  });
}

// ==================== FIRESTORE DATA LOADING ====================
function loadPotholes() {
  showLoading(true);
  
  // Unsubscribe from previous listener if exists
  if (currentUnsubscribe) {
    currentUnsubscribe();
  }
  
  // Build query
  let q = query(
    collection(db, CONFIG.COLLECTION_NAME),
    orderBy('timestamp', 'desc'),
    limit(CONFIG.MAX_POTHOLES)
  );
  
  // Real-time listener
  currentUnsubscribe = onSnapshot(q, 
    (snapshot) => {
      console.log(`📊 Received ${snapshot.size} potholes from Firestore`);
      
      allPotholes = [];
      snapshot.forEach((doc) => {
        const data = doc.data();
        // Using field names from teammate's app: latitude, longitude (not lat, lon)
        if (data.latitude && data.longitude) {
          // Timestamp is milliseconds from app, convert to Date
          let timestamp;
          if (data.timestamp) {
            if (typeof data.timestamp === 'number') {
              timestamp = new Date(data.timestamp);
            } else if (data.timestamp.toDate) {
              timestamp = data.timestamp.toDate();
            } else {
              timestamp = new Date();
            }
          } else {
            timestamp = new Date();
          }
          
          allPotholes.push({
            id: doc.id,
            lat: data.latitude,       // Map to lat for internal use
            lon: data.longitude,      // Map to lon for internal use
            severity: data.severity || 'low',  // String: "low", "medium", "high"
            timestamp: timestamp,
            speed: data.speed,
            acceleration: data.acceleration,
            deviceId: data.deviceId,
            userId: data.userId
          });
        }
      });
      
      applyFiltersAndRender();
      showLoading(false);
    },
    (error) => {
      console.error('❌ Firestore error:', error);
      showError(`Failed to load data: ${error.message}`);
      showLoading(false);
    }
  );
}

// ==================== FILTERING ====================
function applyFiltersAndRender() {
  const severityFilter = elements.severityFilter.value;
  const timeFilter = elements.timeFilter.value;
  
  let filtered = [...allPotholes];
  
  // Filter by severity (using string: "low", "medium", "high")
  if (severityFilter !== 'all') {
    filtered = filtered.filter(p => p.severity === severityFilter);
  }
  
  // Filter by time
  if (timeFilter !== 'all') {
    const now = new Date();
    let cutoff = new Date();
    
    switch(timeFilter) {
      case 'today':
        cutoff.setHours(0, 0, 0, 0);
        break;
      case 'week':
        cutoff.setDate(now.getDate() - 7);
        break;
      case 'month':
        cutoff.setDate(now.getDate() - 30);
        break;
    }
    
    filtered = filtered.filter(p => p.timestamp >= cutoff);
  }
  
  renderMarkers(filtered);
  updateStats(filtered);
}

// ==================== RENDER MARKERS ====================
function renderMarkers(potholes) {
  // Clear existing markers
  markersCluster.clearLayers();
  
  if (potholes.length === 0) {
    console.log('ℹ️ No potholes to display');
    return;
  }
  
  // Create markers
  const markers = potholes.map(pothole => {
    const { lat, lon, severity = 'low', timestamp, speed, acceleration } = pothole;
    const color = CONFIG.COLORS[severity] || CONFIG.COLORS['low'];
    const label = CONFIG.LABELS[severity] || 'Unknown';
    const severityClass = severity;  // Already "low", "medium", or "high"
    
    const marker = L.circleMarker([lat, lon], {
      radius: 8,
      fillColor: color,
      color: '#fff',
      weight: 2,
      opacity: 1,
      fillOpacity: 0.85,
      severity: severity  // Store for cluster icon
    });
    
    // Build popup content with additional data from app
    let extraInfo = '';
    if (speed !== undefined && speed !== null) {
      extraInfo += `<div class="popup-extra">🚗 Speed: ${speed.toFixed(1)} km/h</div>`;
    }
    if (acceleration !== undefined && acceleration !== null) {
      extraInfo += `<div class="popup-extra">📊 Impact: ${acceleration.toFixed(2)} m/s²</div>`;
    }
    
    // Popup content
    const popupContent = `
      <div class="popup-content">
        <div class="popup-title">🕳️ Pothole Detected</div>
        <span class="popup-severity ${severityClass}">${label} Severity</span>
        <div class="popup-time">📅 ${formatDate(timestamp)}</div>
        ${extraInfo}
      </div>
    `;
    
    marker.bindPopup(popupContent);
    
    return marker;
  });
  
  // Add all markers to cluster
  markersCluster.addLayers(markers);
  
  console.log(`✅ Rendered ${markers.length} markers`);
}

// ==================== STATISTICS ====================
function updateStats(potholes) {
  const total = potholes.length;
  // Count by string severity values
  const high = potholes.filter(p => p.severity === 'high').length;
  const medium = potholes.filter(p => p.severity === 'medium').length;
  const low = potholes.filter(p => p.severity === 'low').length;
  
  // Animate counter updates
  animateCounter(elements.totalCount, total);
  animateCounter(elements.highCount, high);
  animateCounter(elements.mediumCount, medium);
  animateCounter(elements.lowCount, low);
  
  // Last update time
  if (potholes.length > 0) {
    const latest = potholes.reduce((a, b) => 
      a.timestamp > b.timestamp ? a : b
    );
    elements.lastUpdate.textContent = formatTimeAgo(latest.timestamp);
  } else {
    elements.lastUpdate.textContent = '--';
  }
}

// Simple counter animation
function animateCounter(element, target) {
  const current = parseInt(element.textContent) || 0;
  if (current === target) return;
  
  const duration = 500;
  const steps = 20;
  const increment = (target - current) / steps;
  let step = 0;
  
  const timer = setInterval(() => {
    step++;
    if (step >= steps) {
      element.textContent = target;
      clearInterval(timer);
    } else {
      element.textContent = Math.round(current + increment * step);
    }
  }, duration / steps);
}

// ==================== UTILITY FUNCTIONS ====================
function formatDate(date) {
  if (!date) return 'Unknown';
  return date.toLocaleString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function formatTimeAgo(date) {
  if (!date) return '--';
  const seconds = Math.floor((new Date() - date) / 1000);
  
  if (seconds < 60) return 'Just now';
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
  return `${Math.floor(seconds / 86400)}d ago`;
}

function showLoading(show) {
  if (show) {
    elements.loading.classList.remove('hidden');
  } else {
    elements.loading.classList.add('hidden');
  }
}

function showError(message) {
  elements.errorText.textContent = message;
  elements.errorMessage.style.display = 'block';
}

// ==================== SEARCH LOCATION ====================
function searchLocation(query) {
  if (!query.trim()) return;
  
  // Use Nominatim (OpenStreetMap) free geocoding API
  const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&countrycodes=in&limit=1`;
  
  fetch(url)
    .then(response => response.json())
    .then(data => {
      if (data && data.length > 0) {
        const result = data[0];
        const lat = parseFloat(result.lat);
        const lon = parseFloat(result.lon);
        
        // Fly to location
        map.flyTo([lat, lon], 13, { duration: 1.5 });
        
        // Add a temporary marker
        const searchMarker = L.marker([lat, lon])
          .addTo(map)
          .bindPopup(`<strong>📍 ${result.display_name.split(',')[0]}</strong><br>${result.display_name}`)
          .openPopup();
        
        // Remove marker after 10 seconds
        setTimeout(() => map.removeLayer(searchMarker), 10000);
        
        console.log(`🔍 Found: ${result.display_name}`);
      } else {
        alert('Location not found. Try a different search term.');
      }
    })
    .catch(error => {
      console.error('Search error:', error);
      alert('Search failed. Please try again.');
    });
}

// ==================== ANALYTICS CHARTS ====================
let severityChart = null;
let timeChart = null;

function openAnalytics() {
  document.getElementById('analyticsModal').classList.remove('hidden');
  renderCharts();
}

function closeAnalytics() {
  document.getElementById('analyticsModal').classList.add('hidden');
}

// Make closeAnalytics available globally
window.closeAnalytics = closeAnalytics;

function renderCharts() {
  renderSeverityChart();
  renderTimeChart();
}

function renderSeverityChart() {
  const ctx = document.getElementById('severityChart').getContext('2d');
  
  const high = allPotholes.filter(p => p.severity === 'high').length;
  const medium = allPotholes.filter(p => p.severity === 'medium').length;
  const low = allPotholes.filter(p => p.severity === 'low').length;
  
  // Destroy existing chart if exists
  if (severityChart) {
    severityChart.destroy();
  }
  
  severityChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['High Severity', 'Medium Severity', 'Low Severity'],
      datasets: [{
        data: [high, medium, low],
        backgroundColor: ['#dc3545', '#fd7e14', '#28a745'],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 20,
            usePointStyle: true
          }
        }
      }
    }
  });
}

function renderTimeChart() {
  const ctx = document.getElementById('timeChart').getContext('2d');
  
  // Get last 7 days data
  const days = [];
  const counts = [];
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date();
    date.setDate(date.getDate() - i);
    const dateStr = date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
    days.push(dateStr);
    
    // Count potholes for this day
    const dayStart = new Date(date.setHours(0, 0, 0, 0));
    const dayEnd = new Date(date.setHours(23, 59, 59, 999));
    
    const count = allPotholes.filter(p => {
      const pDate = p.timestamp;
      return pDate >= dayStart && pDate <= dayEnd;
    }).length;
    
    counts.push(count);
  }
  
  // Destroy existing chart if exists
  if (timeChart) {
    timeChart.destroy();
  }
  
  timeChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: days,
      datasets: [{
        label: 'Potholes Reported',
        data: counts,
        backgroundColor: '#1a73e8',
        borderRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            stepSize: 1
          }
        }
      }
    }
  });
}

// ==================== EVENT LISTENERS ====================
function setupEventListeners() {
  // Filter changes
  elements.severityFilter.addEventListener('change', applyFiltersAndRender);
  elements.timeFilter.addEventListener('change', applyFiltersAndRender);
  
  // Refresh button
  elements.refreshBtn.addEventListener('click', () => {
    console.log('🔄 Manual refresh triggered');
    loadPotholes();
  });
  
  // Search functionality
  const searchInput = document.getElementById('searchInput');
  const searchBtn = document.getElementById('searchBtn');
  
  searchBtn.addEventListener('click', () => {
    searchLocation(searchInput.value);
  });
  
  searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      searchLocation(searchInput.value);
    }
  });
  
  // Analytics button
  const chartsBtn = document.getElementById('chartsBtn');
  chartsBtn.addEventListener('click', openAnalytics);
  
  // Close modal on outside click
  document.getElementById('analyticsModal').addEventListener('click', (e) => {
    if (e.target.id === 'analyticsModal') {
      closeAnalytics();
    }
  });
}

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', () => {
  console.log('🚀 Road-Sense Dashboard initializing...');
  
  try {
    initMap();
    setupEventListeners();
    loadPotholes();
    
    console.log('✅ Dashboard ready!');
  } catch (error) {
    console.error('❌ Initialization failed:', error);
    showError('Failed to initialize dashboard. Please refresh.');
  }
});

// ==================== EXPORTS (for debugging in console) ====================
window.roadSense = {
  map,
  loadPotholes,
  allPotholes: () => allPotholes,
  config: CONFIG,
  searchLocation,
  openAnalytics
};
