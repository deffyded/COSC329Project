# Generated by Django 3.0.14 on 2021-07-18 03:53

import course.fields
from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('course', '0016_auto_20210704_1631'),
    ]

    operations = [
        migrations.AddField(
            model_name='javaquestion',
            name='input_files',
            field=course.fields.JSONField(default=dict),
        ),
    ]
