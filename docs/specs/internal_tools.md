# Internal Tools

The project includes custom tools to automate data management and code generation.

## Country Data Generator

- **Location**: `/tools/countrydata`
- **Purpose**: Generates a standardized list of countries, their ISO codes, dialing prefixes, and flag emojis.
- **Output**: Produces `CountryDefaults.generated.kt` which is used by the Onboarding flow for the phone entry screen.

### How it Works
1.  Uses `libphonenumber` to fetch accurate international dialing codes.
2.  Iterates through all ISO country codes.
3.  Calculates the Regional Indicator Symbol (emoji flag) for each country.
4.  Generates a Kotlin `listOf` containing `CountryOption` objects.

### Usage
This tool is typically run when new country data is needed or when the phone validation logic is updated. It ensures that the UI remains consistent with international standards without hardcoding thousands of lines of data manually.
