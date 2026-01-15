-- ============================================
-- Script SQL para adicionar coluna kiosk_mode
-- ============================================
-- Execute este script no Supabase SQL Editor

-- Adiciona coluna kiosk_mode na tabela devices
ALTER TABLE devices 
ADD COLUMN IF NOT EXISTS kiosk_mode BOOLEAN DEFAULT false;

-- Comentário na coluna
COMMENT ON COLUMN devices.kiosk_mode IS 'Modo kiosk: true = bloqueia minimização do app, false = permite minimizar normalmente';

-- Atualiza dispositivos existentes para false (padrão)
UPDATE devices 
SET kiosk_mode = false 
WHERE kiosk_mode IS NULL;

-- ============================================
-- Exemplos de uso:
-- ============================================

-- Ativar modo kiosk para um dispositivo específico
-- UPDATE devices 
-- SET kiosk_mode = true 
-- WHERE device_id = 'SEU_DEVICE_ID_AQUI';

-- Desativar modo kiosk para um dispositivo específico
-- UPDATE devices 
-- SET kiosk_mode = false 
-- WHERE device_id = 'SEU_DEVICE_ID_AQUI';

-- Verificar status do modo kiosk de todos os dispositivos
-- SELECT device_id, unit_name, kiosk_mode, last_seen 
-- FROM devices 
-- ORDER BY last_seen DESC;

-- Verificar dispositivos com modo kiosk ativo
-- SELECT device_id, unit_name, last_seen 
-- FROM devices 
-- WHERE kiosk_mode = true;
