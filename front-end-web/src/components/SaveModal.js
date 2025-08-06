import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
} from '@mui/material';

function SaveModal({ open, onClose, onSave }) {
  const [fileName, setFileName] = useState("");

  const handleSave = () => {
    if (fileName.trim() !== "") {
      onSave(fileName.trim());
      setFileName("");
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Save Simulation State</DialogTitle>
      <DialogContent>
        <TextField
          autoFocus
          margin="dense"
          label="Filename"
          type="text"
          fullWidth
          value={fileName}
          onChange={(e) => setFileName(e.target.value)}
          helperText="Enter the filename (e.g., simulation.json)"
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="secondary">
          Cancel
        </Button>
        <Button onClick={handleSave} variant="contained" color="primary">
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default SaveModal;