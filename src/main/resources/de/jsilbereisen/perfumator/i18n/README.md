# Adding a resource bundle...

### ... for a Code Perfume

To add a new resource bundle for a Code Pefume, you need to take the following
steps:

1. Create a base resource in the `resources/i18n/perfumes` folder.
2. Check that the name of the base resource **EXACTLY** matches the perfume's name (without the `.properties` file extension of course). 
3. Add all required properties in English to this base resource, as English is the default application language
4. Now you can add support for other languages for your Perfume by adding other resources in the same folder and the same base name, extended with an _underscore_ ( "_" ) and the _language tag_, for example for the base resource/perfume "foo" and the language Spanish, you would add `foo_es.properties`
5. Make sure that if you add a resource for a language, that the language is generally supported by the application, meaning the tag is defined in the `de.jsilbereisen.perfumator.io.LanguageTag` class. If not, feel free to simply add a new constant to the class for your desired language.

### ... for the application itself

TODO