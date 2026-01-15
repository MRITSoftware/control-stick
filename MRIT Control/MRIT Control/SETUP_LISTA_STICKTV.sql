-- Script SQL para criar as tabelas necessárias no Supabase
-- Execute este script no SQL Editor do Supabase

-- ============================================
-- TABELA 1: lista_sticktv (Dispositivos)
-- ============================================

-- Cria a tabela lista_sticktv
CREATE TABLE IF NOT EXISTS lista_sticktv (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_dispositivo TEXT NOT NULL UNIQUE,
    nome TEXT,
    url_pwa TEXT,
    ativo BOOLEAN DEFAULT true,
    kiosk_mode BOOLEAN DEFAULT false,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ultima_atualizacao TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_lista_sticktv_codigo ON lista_sticktv(codigo_dispositivo);
CREATE INDEX IF NOT EXISTS idx_lista_sticktv_ativo ON lista_sticktv(ativo);

-- Comentários nas colunas
COMMENT ON TABLE lista_sticktv IS 'Lista de dispositivos Stick TV para controle remoto';
COMMENT ON COLUMN lista_sticktv.codigo_dispositivo IS 'Código único do dispositivo (Android ID)';
COMMENT ON COLUMN lista_sticktv.nome IS 'Nome da unidade/localização do dispositivo';
COMMENT ON COLUMN lista_sticktv.url_pwa IS 'URL do PWA que deve ser aberto no dispositivo';
COMMENT ON COLUMN lista_sticktv.ativo IS 'Se o dispositivo está ativo';
COMMENT ON COLUMN lista_sticktv.kiosk_mode IS 'Modo kiosk ativo (bloqueia minimização)';

-- ============================================
-- TABELA 2: device_commands (Comandos Remotos)
-- ============================================

-- Cria a tabela device_commands para comandos remotos
CREATE TABLE IF NOT EXISTS device_commands (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id TEXT NOT NULL,
    command TEXT NOT NULL,
    executed BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    executed_at TIMESTAMP WITH TIME ZONE
);

-- Índices para melhorar performance nas consultas
CREATE INDEX IF NOT EXISTS idx_device_commands_device_id ON device_commands(device_id);
CREATE INDEX IF NOT EXISTS idx_device_commands_pending ON device_commands(device_id, command, executed) 
WHERE executed = false;

-- Comentários nas colunas
COMMENT ON TABLE device_commands IS 'Comandos remotos para dispositivos (ex: reiniciar app)';
COMMENT ON COLUMN device_commands.device_id IS 'Código do dispositivo (deve corresponder a codigo_dispositivo em lista_sticktv)';
COMMENT ON COLUMN device_commands.command IS 'Tipo de comando (ex: restart_app)';
COMMENT ON COLUMN device_commands.executed IS 'Se o comando foi executado';
COMMENT ON COLUMN device_commands.created_at IS 'Data de criação do comando';
COMMENT ON COLUMN device_commands.executed_at IS 'Data de execução do comando';

-- ============================================
-- EXEMPLOS DE USO
-- ============================================

-- Exemplo 1: Inserir novo dispositivo com URL
-- INSERT INTO lista_sticktv (codigo_dispositivo, nome, url_pwa, ativo, kiosk_mode)
-- VALUES ('a2674df4a688c7d7', 'Sala 01', 'https://app.muraltv.com.br', true, true);

-- Exemplo 2: Atualizar URL do PWA
-- UPDATE lista_sticktv 
-- SET url_pwa = 'https://app.muraltv.com.br', ultima_atualizacao = NOW()
-- WHERE codigo_dispositivo = 'a2674df4a688c7d7';

-- Exemplo 3: Criar comando para reiniciar app/URL
-- INSERT INTO device_commands (device_id, command, executed)
-- VALUES ('a2674df4a688c7d7', 'restart_app', false);

-- Exemplo 4: Ver comandos pendentes
-- SELECT * FROM device_commands 
-- WHERE device_id = 'a2674df4a688c7d7' 
--   AND command = 'restart_app' 
--   AND executed = false
-- ORDER BY created_at DESC;

-- Exemplo 5: Ver todos os dispositivos
-- SELECT codigo_dispositivo, nome, url_pwa, ativo, kiosk_mode, ultima_atualizacao
-- FROM lista_sticktv
-- ORDER BY ultima_atualizacao DESC;
