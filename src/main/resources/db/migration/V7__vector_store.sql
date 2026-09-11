create extension if not exists hstore;
create extension if not exists "uuid-ossp";

create table vector_store (
    id uuid default uuid_generate_v4() primary key,
    content text,
    metadata json,
    embedding vector(1536)
);

create index idx_vector_store_embedding_hnsw
    on vector_store using hnsw (embedding vector_cosine_ops);
