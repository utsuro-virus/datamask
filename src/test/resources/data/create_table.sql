-- Table: public.m_postal_code

-- DROP TABLE public.m_postal_code;

CREATE TABLE public.m_postal_code
(
    seqno integer NOT NULL DEFAULT nextval('m_postal_code_seqno_seq'::regclass),
    id numeric(9,0),
    ken_id numeric(2,0),
    city_id numeric(5,0),
    town_id numeric(9,0),
    zip character varying COLLATE pg_catalog."default",
    office_flg numeric(1,0),
    delete_flg numeric(1,0),
    ken_name character varying COLLATE pg_catalog."default",
    ken_furi character varying COLLATE pg_catalog."default",
    city_name character varying COLLATE pg_catalog."default",
    city_furi character varying COLLATE pg_catalog."default",
    town_name character varying COLLATE pg_catalog."default",
    town_furi character varying COLLATE pg_catalog."default",
    town_memo character varying COLLATE pg_catalog."default",
    kyoto_street character varying COLLATE pg_catalog."default",
    block_name character varying COLLATE pg_catalog."default",
    block_furi character varying COLLATE pg_catalog."default",
    memo character varying COLLATE pg_catalog."default",
    office_name character varying COLLATE pg_catalog."default",
    office_furi character varying COLLATE pg_catalog."default",
    office_address character varying COLLATE pg_catalog."default",
    new_id numeric(11,0),
    CONSTRAINT pk_m_postal_code PRIMARY KEY (seqno)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.m_postal_code
    OWNER to postgres;

COMMENT ON TABLE public.m_postal_code
    IS '住所データ';

COMMENT ON COLUMN public.m_postal_code.seqno
    IS '連番';

COMMENT ON COLUMN public.m_postal_code.id
    IS '住所コード';

COMMENT ON COLUMN public.m_postal_code.ken_id
    IS '都道府県コード';

COMMENT ON COLUMN public.m_postal_code.city_id
    IS '市区町村コード';

COMMENT ON COLUMN public.m_postal_code.town_id
    IS '町域コード';

COMMENT ON COLUMN public.m_postal_code.zip
    IS '郵便番号';

COMMENT ON COLUMN public.m_postal_code.office_flg
    IS '事業所フラグ';

COMMENT ON COLUMN public.m_postal_code.delete_flg
    IS '廃止フラグ';

COMMENT ON COLUMN public.m_postal_code.ken_name
    IS '都道府県';

COMMENT ON COLUMN public.m_postal_code.ken_furi
    IS '都道府県カナ';

COMMENT ON COLUMN public.m_postal_code.city_name
    IS '市区町村';

COMMENT ON COLUMN public.m_postal_code.city_furi
    IS '市区町村カナ';

COMMENT ON COLUMN public.m_postal_code.town_name
    IS '町域';

COMMENT ON COLUMN public.m_postal_code.town_furi
    IS '町域カナ';

COMMENT ON COLUMN public.m_postal_code.town_memo
    IS '町域補足';

COMMENT ON COLUMN public.m_postal_code.kyoto_street
    IS '京都通り名';

COMMENT ON COLUMN public.m_postal_code.block_name
    IS '字丁目';

COMMENT ON COLUMN public.m_postal_code.block_furi
    IS '字丁目カナ';

COMMENT ON COLUMN public.m_postal_code.memo
    IS '補足';

COMMENT ON COLUMN public.m_postal_code.office_name
    IS '事業所名';

COMMENT ON COLUMN public.m_postal_code.office_furi
    IS '事業所名カナ';

COMMENT ON COLUMN public.m_postal_code.office_address
    IS '事業所住所';

COMMENT ON COLUMN public.m_postal_code.new_id
    IS '新住所CD';

-- Table: public.m_jinmei

-- DROP TABLE public.m_jinmei;

CREATE TABLE public.m_jinmei
(
    name_type character varying(16) COLLATE pg_catalog."default",
    seqno integer,
    kanji character varying(10) COLLATE pg_catalog."default",
    yomi character varying(20) COLLATE pg_catalog."default"
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.m_jinmei
    OWNER to postgres;

COMMENT ON TABLE public.m_jinmei
    IS '人名辞書';

COMMENT ON COLUMN public.m_jinmei.name_type
    IS 'LAST_NAME/FIRST_NAME';

COMMENT ON COLUMN public.m_jinmei.seqno
    IS '連番';

COMMENT ON COLUMN public.m_jinmei.kanji
    IS '漢字';

COMMENT ON COLUMN public.m_jinmei.yomi
    IS '読み仮名';
