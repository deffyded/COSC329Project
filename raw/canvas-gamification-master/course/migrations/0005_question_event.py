# Generated by Django 3.0.7 on 2020-09-04 07:58

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('canvas', '0003_event'),
        ('course', '0004_auto_20200815_0111'),
    ]

    operations = [
        migrations.AddField(
            model_name='question',
            name='event',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.CASCADE, related_name='question_set', to='canvas.Event'),
        ),
    ]
