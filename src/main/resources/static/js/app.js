let token = null;

// Auth Functions
async function login(event) {
    event.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await fetch('/api/auth/signin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) throw new Error('Login failed');

        const data = await response.json();
        token = data.accessToken;
        localStorage.setItem('token', token);
        localStorage.setItem('userName', data.fullName);
        showFileManager();
    } catch (error) {
        alert('Login failed: ' + error.message);
    }
}

async function register(event) {
    event.preventDefault();
    const fullName = document.getElementById('fullName').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;

    try {
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ fullName, email, password })
        });

        if (!response.ok) throw new Error('Registration failed');

        alert('Registration successful! Please login.');
        toggleForms();
    } catch (error) {
        alert('Registration failed: ' + error.message);
    }
}

function logout() {
    token = null;
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    showAuthForms();
}

// UI Functions
function toggleForms() {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    loginForm.style.display = loginForm.style.display === 'none' ? 'block' : 'none';
    registerForm.style.display = registerForm.style.display === 'none' ? 'block' : 'none';
}

function showFileManager() {
    document.getElementById('authForms').style.display = 'none';
    document.getElementById('fileManager').style.display = 'block';
    document.getElementById('userInfo').style.display = 'flex';
    document.getElementById('userName').textContent = localStorage.getItem('userName');
    loadFiles();
}

function showAuthForms() {
    document.getElementById('authForms').style.display = 'block';
    document.getElementById('fileManager').style.display = 'none';
    document.getElementById('userInfo').style.display = 'none';
}

// File Management Functions
async function loadFiles() {
    try {
        const response = await fetch('/api/files', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Failed to load files');

        const files = await response.json();
        displayFiles(files);
    } catch (error) {
        alert('Failed to load files: ' + error.message);
    }
}

function displayFiles(files) {
    const fileList = document.getElementById('files');
    fileList.innerHTML = '';

    files.forEach(file => {
        const fileElement = document.createElement('div');
        fileElement.className = 'file-item';
        fileElement.innerHTML = `
            <span>${file.fileName}</span>
            <div>
                <button onclick="downloadFile(${file.id})">Download</button>
                <button onclick="deleteFile(${file.id})">Delete</button>
            </div>
        `;
        fileList.appendChild(fileElement);
    });
}

// Initialize drag and drop
const dropZone = document.getElementById('dropZone');
const fileInput = document.getElementById('fileInput');

dropZone.addEventListener('click', () => fileInput.click());
dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('dragover');
});
dropZone.addEventListener('dragleave', () => dropZone.classList.remove('dragover'));
dropZone.addEventListener('drop', handleFileDrop);
fileInput.addEventListener('change', handleFileSelect);

function handleFileDrop(e) {
    e.preventDefault();
    dropZone.classList.remove('dragover');
    const files = e.dataTransfer.files;
    uploadFiles(files);
}

function handleFileSelect(e) {
    const files = e.target.files;
    uploadFiles(files);
}

async function uploadFiles(files) {
    for (const file of files) {
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch('/api/files/upload', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (!response.ok) throw new Error('Upload failed');
            
            loadFiles(); // Refresh file list
        } catch (error) {
            alert(`Failed to upload ${file.name}: ${error.message}`);
        }
    }
}

async function downloadFile(fileId) {
    try {
        const response = await fetch(`/api/files/${fileId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Download failed');

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = response.headers.get('Content-Disposition').split('filename=')[1].replace(/"/g, '');
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
    } catch (error) {
        alert('Download failed: ' + error.message);
    }
}

async function deleteFile(fileId) {
    if (!confirm('Are you sure you want to delete this file?')) return;

    try {
        const response = await fetch(`/api/files/${fileId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Delete failed');

        loadFiles(); // Refresh file list
    } catch (error) {
        alert('Delete failed: ' + error.message);
    }
}

// Check if user is already logged in
window.onload = () => {
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
        token = savedToken;
        showFileManager();
    }
}; 