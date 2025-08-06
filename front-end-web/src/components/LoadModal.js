import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
} from '@mui/material';

function LoadModal({ open, onClose, onLoad }) {
  const [file, setFile] = useState(null);
  const [error, setError] = useState('');

  const handleFileChange = (event) => {
    if (event.target.files && event.target.files[0]) {
      setFile(event.target.files[0]);
      setError('');
    }
  };

  // Read the file as text and try to parse JSON
  const handleLoad = () => {
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const jsonData = JSON.parse(event.target.result);
        onLoad(jsonData); 
        onClose();        
      } catch (err) {
        setError('Error parsing JSON file. Please ensure the file is valid JSON.');
        console.error('Error parsing JSON:', err);
      }
    };
    reader.onerror = () => {
      setError('Error reading file.');
    };
    reader.readAsText(file);
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Load Simulation File</DialogTitle>
      <DialogContent>
        <input type="file" accept=".json,application/json" onChange={handleFileChange} />
        {error && (
          <TextField
            error
            value={error}
            helperText={error}
            fullWidth
            disabled
            variant="standard"
          />
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleLoad} color="primary" variant="contained" disabled={!file}>
          Load
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default LoadModal;
