
CREATE TABLE cas2_demand (
    id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    decided_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    identifier TEXT NOT NULL,

    location_type TEXT NOT NULL,
    location TEXT NOT NULL,

    primary_reason TEXT NOT NULL,
    secondary_reason TEXT,

    PRIMARY KEY (id)
);
