import React, { useState } from 'react';
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  Button, TextField, FormControl, InputLabel, Select,
  MenuItem, Box
} from '@mui/material';

export default function NewBuildingModal({ open, onClose, onCreate }) {
  const [buildingType, setBuildingType] = useState('');
  const [buildingName, setBuildingName] = useState('');
  const [coordinateX, setCoordinateX] = useState('');
  const [coordinateY, setCoordinateY] = useState('');
  const [info, setInfo] = useState({
    stores: '',      
    capacity: '',    
    priority: '',    
    mine: '',        
    recipes: '',     
    disposeAmount: '',
    disposeInterval: '',
    wasteTypes: '',
    initialDroneCount: '',
  });

  const handleSave = () => {
    // build the base info object
    const nestedInfo = {
      coordinate: [
        parseFloat(coordinateX, 10) || 0,
        parseFloat(coordinateY, 10) || 0,
      ],
    };
  
    switch (buildingType) {
      case 'storage':
        nestedInfo.stores    = info.stores;
        nestedInfo.capacity  = parseInt(info.capacity, 10);
        nestedInfo.priority  = parseFloat(info.priority);
        break;
  
      case 'mine':
        nestedInfo.mine = info.mine;
        break;
  
      case 'factory': {
        const recipesArray = info.recipes
          .split(',')
          .map((s) => s.trim())
          .filter(Boolean);
        if (recipesArray.length === 0) {
          alert('Please provide at least one recipe.');
          return;
        }
        nestedInfo.type    = recipesArray[0];
        nestedInfo.recipes = recipesArray;
        break;
      }
  
      case 'waste disposal':
        nestedInfo.disposeAmount   = parseInt(info.disposeAmount, 10);
        nestedInfo.capacity        = parseInt(info.capacity, 10);
        nestedInfo.disposeInterval = parseInt(info.disposeInterval, 10);
        nestedInfo.wasteTypes      = info.wasteTypes
          .split(',')
          .map((w) => w.trim())
          .filter(Boolean);
        break;
  
      case 'drone port':
        nestedInfo.initialDroneCount = parseInt(info.initialDroneCount, 10);
        break;  
      default:
        alert('Please select a building type');
        return;
    }
  
    // build and send the payload
    const payload = {
      type: buildingType,
      name: buildingName,
      info: nestedInfo,
    };
    console.log('>>> newBuilding payload:', payload);
    onCreate(payload);
  
    // reset the form
    setBuildingType('');
    setBuildingName('');
    setCoordinateX('');
    setCoordinateY('');
    setInfo({
      stores: '', capacity: '', priority: '',
      mine: '', recipes: '',
      disposeAmount: '', disposeInterval: '', wasteTypes: '',
      initialDroneCount: ''
    });
  };  

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Create New Building</DialogTitle>
      <DialogContent>
        <FormControl fullWidth margin="normal">
          <InputLabel>Building Type</InputLabel>
          <Select
            value={buildingType}
            onChange={e => setBuildingType(e.target.value)}
          >
            <MenuItem value="factory">Factory</MenuItem>
            <MenuItem value="mine">Mine</MenuItem>
            <MenuItem value="storage">Storage</MenuItem>
            <MenuItem value="waste disposal">Waste Disposal</MenuItem>
            <MenuItem value="drone port">Drone Port</MenuItem>
          </Select>
        </FormControl>

        <TextField
          fullWidth margin="normal"
          label="Building Name"
          value={buildingName}
          onChange={e => setBuildingName(e.target.value)}
        />

        <Box display="flex" gap={2}>
          <TextField
            fullWidth margin="normal" type="number"
            label="Coordinate X"
            value={coordinateX}
            onChange={e => setCoordinateX(e.target.value)}
          />
          <TextField
            fullWidth margin="normal" type="number"
            label="Coordinate Y"
            value={coordinateY}
            onChange={e => setCoordinateY(e.target.value)}
          />
        </Box>

        {buildingType === 'storage' && (
          <>
            <TextField
              fullWidth margin="normal"
              label="Stores (ingredient)"
              value={info.stores}
              onChange={e => setInfo({ ...info, stores: e.target.value })}
            />
            <TextField
              fullWidth margin="normal" type="number"
              label="Capacity"
              value={info.capacity}
              onChange={e => setInfo({ ...info, capacity: e.target.value })}
            />
            <TextField
              fullWidth margin="normal" type="number"
              label="Priority"
              value={info.priority}
              onChange={e => setInfo({ ...info, priority: e.target.value })}
            />
          </>
        )}

        {buildingType === 'mine' && (
          <TextField
            fullWidth margin="normal"
            label="Mine (ingredient)"
            value={info.mine}
            onChange={e => setInfo({ ...info, mine: e.target.value })}
          />
        )}

        {buildingType === 'factory' && (
          <TextField
            fullWidth margin="normal"
            label="Recipes (comma-separated)"
            value={info.recipes}
            onChange={e => setInfo({ ...info, recipes: e.target.value })}
          />
        )}

        {buildingType === 'waste disposal' && (
          <>
            <TextField
              fullWidth margin="normal" type="number"
              label="Dispose Amount"
              value={info.disposeAmount}
              onChange={e => setInfo({ ...info, disposeAmount: e.target.value })}
            />
            <TextField
              fullWidth margin="normal" type="number"
              label="Capacity"
              value={info.capacity}
              onChange={e => setInfo({ ...info, capacity: e.target.value })}
            />
            <TextField
              fullWidth margin="normal" type="number"
              label="Dispose Interval"
              value={info.disposeInterval}
              onChange={e => setInfo({ ...info, disposeInterval: e.target.value })}
            />
            <TextField
              fullWidth margin="normal"
              label="Waste Types (comma-separated)"
              value={info.wasteTypes}
              onChange={e => setInfo({ ...info, wasteTypes: e.target.value })}
            />
          </>
        )}

        {buildingType === 'drone port' && (
          <TextField
            fullWidth margin="normal" type="number"
            label="Initial Drone Count"
            value={info.initialDroneCount}
            onChange={e => setInfo({ ...info, initialDroneCount: e.target.value })}
          />
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} color="secondary">Cancel</Button>
        <Button onClick={handleSave} variant="contained" color="primary">
          Create
        </Button>
      </DialogActions>
    </Dialog>
  );
}
