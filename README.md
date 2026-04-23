---

## Natural Language Parsing

### Approach
The parser uses rule-based keyword matching. The query string is lowercased and scanned for known keywords. No AI or LLMs are used.

### Supported Keywords and Mappings

**Gender:**
| Keyword | Maps To |
|---|---|
| `male` | gender = male |
| `female` | gender = female |

**Age Groups:**
| Keyword | Maps To |
|---|---|
| `child`, `children` | age_group = child |
| `teenager` | age_group = teenager |
| `adult` | age_group = adult |
| `senior` | age_group = senior |

**Special Age Ranges:**
| Keyword | Maps To |
|---|---|
| `young` | min_age = 16, max_age = 24 (not a stored age_group) |
| `above X` | min_age = X (e.g. "above 30" → min_age = 30) |

**Countries (partial list):**
| Keyword | Maps To |
|---|---|
| `nigeria` | country_id = NG |
| `kenya` | country_id = KE |
| `ghana` | country_id = GH |
| `angola` | country_id = AO |
| `benin` | country_id = BJ |
| `cameroon` | country_id = CM |
| `senegal` | country_id = SN |
| `ethiopia` | country_id = ET |
| `tanzania` | country_id = TZ |
| `uganda` | country_id = UG |
| `south africa` | country_id = ZA |
| `ivory coast` | country_id = CI |
| `mozambique` | country_id = MZ |
| `niger` | country_id = NE |
| `mali` | country_id = ML |
| `togo` | country_id = TG |
| `guinea` | country_id = GN |
| `zambia` | country_id = ZM |
| `zimbabwe` | country_id = ZW |
| `burkina faso` | country_id = BF |

### Example Query Mappings
| Query | Parsed Filters |
|---|---|
| `young males from nigeria` | gender=male, min_age=16, max_age=24, country_id=NG |
| `females above 30` | gender=female, min_age=30 |
| `adult males from kenya` | gender=male, age_group=adult, country_id=KE |
| `teenagers` | age_group=teenager |
| `senior females` | gender=female, age_group=senior |
| `people from angola` | country_id=AO |

### How the Logic Works
1. Query is lowercased and trimmed
2. Gender keywords are checked first
3. Age group keywords are checked next
4. `young` is checked and sets a min/max age range (16-24) independently of age_group
5. `above X` pattern is matched using regex to extract a minimum age
6. Country name keywords are checked against a lookup map
7. If none of the above produce any filter, the query is considered uninterpretable and returns an error

### Error Response for Uninterpretable Queries
```json
{
  "status": "error",
  "message": "Unable to interpret query"
}
```

---

## Limitations

- **Country coverage is partial** — only ~20 African countries are mapped. Queries like "people from france" or "males from brazil" will not match a country filter and may return an uninterpretable error unless another keyword is present.
- **No OR logic** — queries like "males or females" are not supported. Only AND logic is applied across all detected filters.
- **`young` is not a stored age group** — it maps to ages 16-24 for query purposes only. The database stores `child`, `teenager`, `adult`, `senior`.
- **No negation** — queries like "not from nigeria" or "excluding seniors" are not supported.
- **No compound age ranges** — queries like "between 20 and 30" are not supported. Only `above X` and `young` are handled.
- **Single country per query** — if multiple countries are mentioned, only the first match is used.
- **No fuzzy matching** — country names must match exactly. "Naija" for Nigeria or "SA" for South Africa will not be recognized.
- **No name search** — querying by specific person names is not supported.

---

## Tech Stack
- Java 21
- Spring Boot 4.x
- PostgreSQL (Neon)
- Spring Data JPA with Specifications
- UUID v7 via uuid-creator library
- Deployed on PXXL
