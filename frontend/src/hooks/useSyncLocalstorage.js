import { useSyncExternalStore } from "react";


export default function useSyncLocalstorage(key, initialValue=null) {
    let lastSnapshot;
    const getSnapshot = () => {
        const currentValue = localStorage.getItem(key);
        if (currentValue !== JSON.stringify(lastSnapshot)) {
            lastSnapshot = JSON.parse(currentValue);
        }
        return lastSnapshot;
    }
    const subscribe = (callback) => {
        window.addEventListener('storage', callback);
        return () => window.removeEventListener('storage', callback);
    }

    if (!localStorage.getItem(key) && initialValue) {
        localStorage.setItem(key, JSON.stringify(initialValue));
    }
    const storedValue = useSyncExternalStore(subscribe, getSnapshot);

    const setValue = (newValue) => {
        newValue = JSON.stringify(newValue);
        localStorage.setItem(key, newValue);
        window.dispatchEvent(new StorageEvent('storage', {key, newValue}));
    }

    const removeValue = () => {
        localStorage.removeItem(key);
        window.dispatchEvent(new StorageEvent('storage', {key: key, newValue: null}));
    }
    
    return [storedValue, setValue, removeValue];
}