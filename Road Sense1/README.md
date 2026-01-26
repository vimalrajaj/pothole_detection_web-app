# 🛣️ Road-Sense Web Dashboard

Real-time crowdsourced pothole mapping dashboard for smarter cities.

## 📋 Overview

This web dashboard displays all potholes reported by Road-Sense mobile app users on an interactive map. It features:

- **Real-time updates** — See new pothole reports appear instantly
- **Severity filtering** — Filter by Low/Medium/High severity
- **Time filtering** — View reports from today, last week, or all time
- **Marker clustering** — Handles thousands of markers efficiently
- **Statistics panel** — Quick overview of total reports and breakdown by severity
- **Responsive design** — Works on desktop, tablet, and mobile

## 🚀 Quick Start

### Prerequisites

1. **Node.js 18+** installed → [Download](https://nodejs.org)
2. **Firebase CLI** installed:
   ```bash
   npm install -g firebase-tools
   ```
3. **Access to Firebase project** (same as Android app)

### Setup Steps

1. **Clone/download this project**

2. **Get Firebase credentials:**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Select your project
   - Project Settings → Your Apps → Web app
   - Copy the `firebaseConfig` object

3. **Update configuration:**
   - Open `public/script.js`
   - Replace the `firebaseConfig` values with yours:
   ```javascript
   const firebaseConfig = {
     apiKey: "YOUR_ACTUAL_API_KEY",
     authDomain: "your-project.firebaseapp.com",
     projectId: "your-project-id",
     storageBucket: "your-project.appspot.com",
     messagingSenderId: "123456789",
     appId: "1:123456789:web:abc123"
   };
   ```

4. **Update project ID:**
   - Open `.firebaserc`
   - Replace `YOUR_PROJECT_ID` with your actual project ID

5. **Login to Firebase:**
   ```bash
   firebase login
   ```

6. **Deploy Firestore rules:**
   ```bash
   firebase deploy --only firestore:rules
   ```

7. **Test locally:**
   ```bash
   firebase serve
   ```
   Open http://localhost:5000 in browser

8. **Deploy to production:**
   ```bash
   firebase deploy --only hosting
   ```

## 📁 Project Structure

```
roadsense-web/
├── public/                 # Hosted files
│   ├── index.html         # Main HTML page
│   ├── style.css          # All styles
│   └── script.js          # Firebase + Map logic
├── firebase.json          # Firebase hosting config
├── firestore.rules        # Security rules
├── .firebaserc            # Project settings
└── README.md              # This file
```

## 🔧 Configuration

### Change Map Center (for different cities)

In `public/script.js`, update `CONFIG.MAP_CENTER`:

```javascript
const CONFIG = {
  // Mumbai
  MAP_CENTER: [19.0760, 72.8777],
  
  // Or use these for other cities:
  // Delhi: [28.6139, 77.2090]
  // Bangalore: [12.9716, 77.5946]
  // Chennai: [13.0827, 80.2707]
  // Kolkata: [22.5726, 88.3639]
};
```

### Change Collection Name

If your Android app uses a different collection name:

```javascript
COLLECTION_NAME: 'potholes',  // Change this
```

## 🔒 Security

The Firestore rules in `firestore.rules`:
- ✅ Allow anyone to **read** potholes (public dashboard)
- ✅ Only authenticated users can **create** reports
- ✅ Data validation ensures proper lat/lon/severity values
- ❌ No one can update or delete reports (data integrity)

## 🎨 Customization

### Colors

Edit `style.css` variables:

```css
:root {
  --primary-color: #1a73e8;    /* Header/buttons */
  --danger-color: #dc3545;     /* High severity */
  --warning-color: #fd7e14;    /* Medium severity */
  --success-color: #28a745;    /* Low severity */
}
```

### Severity Markers

Edit `CONFIG.COLORS` in `script.js`:

```javascript
COLORS: {
  1: '#28a745',  // Low
  2: '#fd7e14',  // Medium
  3: '#dc3545'   // High
}
```

## 📊 Expected Firestore Document Structure

Each document in the `potholes` collection should have:

```javascript
{
  lat: 19.0760,           // Number: latitude
  lon: 72.8777,           // Number: longitude
  severity: 2,            // Number: 1=Low, 2=Medium, 3=High
  timestamp: Timestamp,   // Firebase Timestamp
  userId: "device_abc123" // String: user/device identifier
}
```

## 🐛 Troubleshooting

### "Firebase not defined" error
- Check that the Firebase SDK scripts are loading in `index.html`
- Check browser console for network errors

### Map not showing
- Check that Leaflet CSS and JS are loading
- Verify the map container `#map` has height in CSS

### No data appearing
- Verify Firestore collection name matches
- Check Firebase Console → Firestore to see if data exists
- Check browser console for permission errors

### CORS errors
- Make sure you're accessing via `localhost` or the deployed URL
- Don't open `index.html` directly as a file

## 📱 Integration with Android App

Make sure your Android app:
1. Uses the **same Firebase project**
2. Uploads to the **same collection** (`potholes`)
3. Includes **all required fields**: `lat`, `lon`, `severity`, `timestamp`, `userId`
4. Uses **Firebase Authentication** (even anonymous) before writing

## 📈 Future Improvements

- [ ] Heatmap visualization mode
- [ ] Export data as CSV
- [ ] Area-wise statistics
- [ ] User leaderboard
- [ ] Push notifications for new reports in area

## 📄 License

MIT License - Feel free to use for your hackathon/project!

---

Built with ❤️ for Smart City Initiative 2026
